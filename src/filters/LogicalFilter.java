package filters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogicalFilter extends Filter{
    private Operator operator;
    private List<Filter> filters;

    @Override
    public String toSQL() {
        String sql = String.join(" " + operator.getSymbol() + " ",
                filters.stream().map(f -> "(" + f.toSQL() + ")").toList());
        return "(" + sql + ")";
    }

    @Override
    public List<Object> getParameters() {
        List<Object> params = new ArrayList<>();
        for (Filter f : filters) {
            params.addAll(f.getParameters());
        }
        return params;
    }

    public LogicalFilter(Operator operator, Filter... filters) {
        this.operator = operator;
        this.filters = Arrays.asList(filters);
    }
}
