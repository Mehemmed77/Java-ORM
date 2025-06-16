package filters;

import java.util.List;

public class ComparisonFilter extends Filter {
    private String columnName;
    private Operator operator;
    private Object value;

    @Override
    public String toSQL() {
        return columnName + " " + operator.getSymbol() + " ? ";
    }

    @Override
    public List<Object> getParameters() {
        return List.of(value);
    }

    public ComparisonFilter(String columnName, Operator operator, Object value){
        this.columnName = columnName;
        this.operator = operator;
        this.value = value;
    }
}
