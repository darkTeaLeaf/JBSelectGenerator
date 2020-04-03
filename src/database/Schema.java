package database;

import java.util.List;

/**
 * Schema instance contains information
 * about schema name and {@code List}
 * of tables in it
 *
 * @author Arina Fedorovskaya
 */

public class Schema {
    private String name;
    private List<Table> tables;

    public String getName() {
        return name;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public Table getTableByName(String name){
        for (Table table: this.tables) {
            if(table.getName().equals(name)){
                return table;
            }
        }

        return null;
    }
}
