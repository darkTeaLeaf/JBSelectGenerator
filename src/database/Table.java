package database;

import java.util.List;

/**
 * Table instance contains information
 * about table name and {@code List}
 * of columns in it
 *
 * @author Arina Fedorovskaya
 */

public class Table {
    private String name;
    private List<Column> columns;

    public String getName() {
        return name;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
