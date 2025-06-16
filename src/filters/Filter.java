package filters;

import java.util.List;

public abstract class Filter {
    public abstract String toSQL();
    public abstract List<Object> getParameters();

    public static Filter eq(String columnName, Object value) {
        return new ComparisonFilter(columnName, Operator.EQUALS, value);
    }

    public static Filter gt(String columnName, Object value) {
        return new ComparisonFilter(columnName, Operator.GREATER_THAN, value);
    }

    public static Filter lt(String columnName, Object value) {
        return new ComparisonFilter(columnName, Operator.LESS_THAN, value);
    }

    public static Filter gte(String columnName, Object value) {
        return new ComparisonFilter(columnName, Operator.GREATER_THAN_OR_EQUALS, value);
    }

    public static Filter lte(String columnName, Object value) {
        return new ComparisonFilter(columnName, Operator.LESS_THAN_OR_EQUALS, value);
    }

    public static Filter and(Filter... filters) {
        return new LogicalFilter(Operator.AND, filters);
    }

    public static Filter or(Filter... filters) {
        return new LogicalFilter(Operator.OR, filters);
    }

    public static Filter not(Filter... filters) {
        return new LogicalFilter(Operator.NOT, filters);
    }
}
