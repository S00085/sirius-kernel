package sirius.kernel.commons

import sirius.kernel.nls.NLS
import spock.lang.Specification

class ValueSpec extends Specification {

    private static final BigDecimal DEFAULT_BIG_DECIMAL = BigDecimal.TEN

    def "Test isFilled"() {
        expect:
        Value.of(input).isFilled() == output
        where:
        input  | output
        1      | true
        " "    | true
        "Test" | true
        ""     | false
        null   | false
    }

    def "Test isNumeric"() {
        expect:
        Value.of(input).isNumeric() == output
        where:
        input   | output
        1       | true
        "1"     | true
        -1      | true
        "-1"    | true
        0       | true
        "0"     | true
        1.1     | true
        "1.1"   | true
        "1.1.1" | false
        ""      | false
        null    | false
        "Test"  | false
    }

    def "Test afterLast"() {
        expect:
        Value.of(input).afterLast(separator) == output
        where:
        input        | separator | output
        "test.x.pdf" | "."       | "pdf"
    }

    def "Test beforeLast"() {
        expect:
        Value.of(input).beforeLast(separator) == output
        where:
        input        | separator | output
        "test.x.pdf" | "."       | "test.x"
    }

    def "Test afterFirst"() {
        expect:
        Value.of(input).afterFirst(separator) == output
        where:
        input        | separator | output
        "test.x.pdf" | "."       | "x.pdf"
    }

    def "Test beforeFirst"() {
        expect:
        Value.of(input).beforeFirst(separator) == output
        where:
        input        | separator | output
        "test.x.pdf" | "."       | "test"
    }

    def "Test left"() {
        expect:
        Value.of(input).left(length) == output
        where:
        input         | length | output
        "testA.testB" | 5      | "testA"
        "testA.testB" | -5     | ".testB"
        "test"        | 5      | "test"
        null          | 5      | ""
    }

    def "Test right"() {
        expect:
        Value.of(input).right(length) == output
        where:
        input         | length | output
        "testA.testB" | 5      | "testB"
        "testA.testB" | -5     | "testA."
        "test"        | 5      | "test"
        null          | 5      | ""
    }

    def "Test getBigDecimal"() {
        expect:
        Value.of(input).getBigDecimal() == output
        where:
        input               | output
        ""                  | null
        "Not a Number"      | null
        "42"                | BigDecimal.valueOf(42)
        "42.0"              | BigDecimal.valueOf(42)
        "42,0"              | BigDecimal.valueOf(42)
        42                  | BigDecimal.valueOf(42)
        42.0                | BigDecimal.valueOf(42)
        Integer.valueOf(42) | BigDecimal.valueOf(42)
    }

    def "Test getBigDecimal with default"() {
        expect:
        Value.of(input).getBigDecimal(DEFAULT_BIG_DECIMAL) == output
        where:
        input               | output
        ""                  | DEFAULT_BIG_DECIMAL
        "Not a Number"      | DEFAULT_BIG_DECIMAL
        "42"                | BigDecimal.valueOf(42)
        "42.0"              | BigDecimal.valueOf(42)
        "42,0"              | BigDecimal.valueOf(42)
        42                  | BigDecimal.valueOf(42)
        42.0                | BigDecimal.valueOf(42)
        Integer.valueOf(42) | BigDecimal.valueOf(42)
    }

    def "Test asBoolean with default"() {
        expect:
        Value.of(input).asBoolean(false) == output
        where:
        input              | output
        ""                 | false
        "false"            | false
        "true"             | true
        false              | false
        true               | true
        NLS.get("NLS.Yes") | true
        NLS.get("NLS.No")  | false
    }

    def "Test coerce boolean and without default"() {
        expect:
        Value.of(input).coerce(boolean.class, null) == output
        where:
        input              | output
        ""                 | false
        "false"            | false
        "true"             | true
        false              | false
        true               | true
        NLS.get("NLS.Yes") | true
        NLS.get("NLS.No")  | false
    }
}
