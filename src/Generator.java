import database.Column;
import database.Database;
import database.Schema;

import database.Table;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Generator {
    private Constructor constructor;
    private Database database;

    Generator() {
        this.constructor = new Constructor(Database.class);
        TypeDescription customTypeDescription = new TypeDescription(Database.class);
        customTypeDescription.addPropertyParameters("schemas", Schema.class);
        customTypeDescription.addPropertyParameters("tables", Table.class);
        customTypeDescription.addPropertyParameters("columns", Column.class);
        this.constructor.addTypeDescription(customTypeDescription);
    }

    public void load(String filepath) {
        Yaml yaml = new Yaml(this.constructor);
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream(filepath);

        this.database = yaml.load(inputStream);
    }

    /**
     * tablePattern - regexp of qualified table name
     * e.g. "sakila\.actor ", "sakila\..*", ".*\.person"
     * query - text to find in database e.g. "Alice", "42", "true"
     * caseSensitive - whether to use LIKE or ILIKE operation for varchar columns
     */
    public List<String> generateSelects(String tablePattern, String query, boolean caseSensitive) {
        if (!checkTablePattern(tablePattern)) {
            throw new RuntimeException("syntax of table pattern is incorrect");
        }

        String[] names = tablePattern.split("\\\\.");
        ArrayList<Table> tables = getTablesList(names);

        if (tables.isEmpty()) {
            throw new RuntimeException("there is no tables matches pattern");
        }

        String like = caseSensitive ? "ILIKE" : "LIKE";

        String typeQuery = determineType(query);
        ArrayList<String> selects = new ArrayList<>();

        for (Table table : tables) {
            StringBuilder partAfterWhere = new StringBuilder();

            for (Column column : table.getColumns()) {
                if (column.getType().matches("varchar\\([0-9]+\\)")) {
                    if(partAfterWhere.length() != 0){
                        partAfterWhere.append(" OR ");
                    }

                    partAfterWhere.append(column.getName()).append(" ").append(like).append(" '%")
                            .append(query).append("%'");
                } else {
                    if (column.getType().equals(typeQuery)) {
                        if(partAfterWhere.length() != 0){
                            partAfterWhere.append(" OR ");
                        }

                        partAfterWhere.append(column.getName()).append(" = ").append(query);
                    }
                }
            }

            if (partAfterWhere.length() != 0) {
                String select = "SELECT * FROM " + table.getName() + " WHERE " + partAfterWhere;
                selects.add(select);
            }
        }

        return selects;
    }

    private boolean checkTablePattern(String tablePattern) {
        return tablePattern.matches("^([a-zA-z]+|\\.\\*)\\\\.([a-zA-z]+|\\.\\*)$");
    }

    private ArrayList<Table> getTablesList(String[] names) {
        ArrayList<Table> tables = new ArrayList<>();

        if (names[0].equals(".*")) {
            if (names[1].equals(".*")) {
                for (Schema schema : this.database.getSchemas()) {
                    tables.addAll(schema.getTables());
                }
            } else {
                for (Schema schema : this.database.getSchemas()) {
                    Table table = schema.getTableByName(names[1]);
                    if (table != null) {
                        tables.add(table);
                    }
                }
            }
        } else {
            Schema schema = this.database.getSchemaByName(names[0]);

            if (schema != null) {
                if (names[1].equals(".*")) {
                    tables.addAll(schema.getTables());
                } else {
                    Table table = schema.getTableByName(names[1]);

                    if (table != null) {
                        tables.add(table);
                    }
                }
            }
        }

        return tables;
    }

    private String determineType(String query) {
        if (query.matches("^[0-9]+$")) {
            return "integer";
        }

        if (query.matches("^(true|false)$")) {
            return "boolean";
        }

        if (query.matches("^((18|19|20)[0-9]{2}-(0[1-9]|[12][0-9]|3[01])-(0[13578]|1[02]))" +
                "|((18|19|20)[0-9]{2}-(0[1-9]|[12][0-9]|30)-(0[469]|11))" +
                "|((18|19|20)[0-9]{2}-(0[1-9]|1[0-9]|2[0-8])-(02))|" +
                "((((18|19|20)(04|08|[2468][048]|[13579][26]))|2000)-29-(02))$")) {
            return "date";
        }

        return "varchar";
    }
}
