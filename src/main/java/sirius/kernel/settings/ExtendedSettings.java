/*
 * Made with all the love in the world
 * by scireum in Remshalden, Germany
 *
 * Copyright by scireum GmbH
 * http://www.scireum.de - info@scireum.de
 */

package sirius.kernel.settings;

import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides an advanced wrapper for a {@link Config} object, which supports to represent inner maps as {@link Extension
 * extensions}.
 * <p>
 * Using the <a href="https://github.com/typesafehub/config" target="_blank">typesafe config library</a>,
 * several several sources can be loaded into a single config object. These sources will be merged together, so that
 * extensions like the following will be put into one extension list reachable as "examples":
 * <pre>
 * {@code
 *     File A:
 *
 *      examples {
 *          A = {
 *              key = "string"
 *              otherKey = true
 *          }
 *      }
 *
 *     File B:
 *
 *      examples {
 *          B = {
 *              key = "string"
 *              otherKey = true
 *          }
 *      }
 * }
 * </pre>
 * <p>
 * This permits frameworks to provide extension hooks which can be extended by sub modules, without having the
 * framework to "know" those modules. Using a loose coupled approach like this simplifies the task of building
 * modular and extensible systems.
 * <p>
 * The extensions defined above can be obtained calling {@code settings.getExtension("examples")}. Each
 * of those extensions can be read out calling {@code ext.getValue("key").asString()} or
 * {@code ext.getValue("otherKey").asBoolean()}
 *
 * @see Extension
 */
public class ExtendedSettings extends Settings {

    /*
     * Used as cache for already loaded extension lists
     */
    private Map<String, Map<String, Extension>> cache = new ConcurrentHashMap<>();

    /*
     * Used as cache for the default values of a given extension type
     */
    private Map<String, Extension> defaultsCache = new ConcurrentHashMap<>();

    public ExtendedSettings(Config config) {
        super(config);
    }

    /**
     * Returns the <tt>Extension</tt> for the given <tt>id</tt> of the given <tt>type</tt>
     *
     * @param type the type of the extension to be returned
     * @param id   the unique id of the extension to be returned
     * @return the specified extension or <tt>null</tt>, if no such extension exists
     */
    @Nullable
    public Extension getExtension(String type, String id) {
        if (!id.matches("[a-z0-9\\-]+")) {
            Extension.LOG.WARN(
                    "Bad extension id detected: '%s' (for type: %s). Names should only consist of lowercase letters,"
                    + " "
                    + "digits or '-'",
                    id,
                    type);
        }

        Extension result = getExtensionMap(type).get(id);
        if (result == null) {
            return getDefault(type);
        }

        return result;
    }

    /**
     * Returns all extensions available for the given type
     * <p>
     * The order of the extensions can be defined, setting a property named <tt>priority</tt>. If no value is
     * present {@link sirius.kernel.commons.PriorityCollector#DEFAULT_PRIORITY} is assumed.
     *
     * @param type the type of the extensions to be returned.
     * @return a non-null collection of extensions found for the given type
     */
    @Nonnull
    public Collection<Extension> getExtensions(String type) {
        return getExtensionMap(type).values();
    }

    protected Extension getDefault(String type) {
        Extension result = defaultsCache.get(type);
        if (result != null) {
            return result;
        }

        ConfigObject cfg = getConfig().getConfig(type).root();
        ConfigObject def = (ConfigObject) cfg.get(Extension.DEFAULT);
        if (cfg.containsKey(Extension.DEFAULT)) {
            result = new Extension(type, Extension.DEFAULT, def, null);
            defaultsCache.put(type, result);
            return result;
        }

        return null;
    }

    private Map<String, Extension> getExtensionMap(String type) {
        Map<String, Extension> result = cache.get(type);
        if (result != null) {
            return result;
        }

        if (!getConfig().hasPath(type)) {
            return Collections.emptyMap();
        }
        ConfigObject cfg = getConfig().getConfig(type).root();
        List<Extension> list = new ArrayList<>();
        ConfigObject defaultObject = null;
        if (cfg.containsKey(Extension.DEFAULT)) {
            defaultObject = (ConfigObject) cfg.get(Extension.DEFAULT);
        }
        for (Map.Entry<String, ConfigValue> entry : cfg.entrySet()) {
            String key = entry.getKey();
            if (!Extension.DEFAULT.equals(key) && !key.contains(".")) {
                if (entry.getValue() instanceof ConfigObject) {
                    list.add(new Extension(type, key, (ConfigObject) entry.getValue(), defaultObject));
                } else {
                    Extension.LOG.WARN("Malformed extension within '%s'. Expected a config object but found: %s",
                                       type,
                                       entry.getValue());
                }
            }
        }

        Collections.sort(list);
        result = Maps.newLinkedHashMap();
        for (Extension ex : list) {
            result.put(ex.getId(), ex);
        }
        cache.put(type, result);
        return result;
    }
}
