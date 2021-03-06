package nl.paulinternet.gtasaveedit.cli;

import nl.paulinternet.libsavegame.Util;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private List<String[]> values;
    private int[] maxSize;
    private int columns;

    /**
     * Creates a new table
     */
    public Table(int columns) {
        // Check the argument
        if (columns < 1)
            throw new IllegalArgumentException();

        // Set the variables
        this.columns = columns;
        maxSize = new int[columns];
        values = new ArrayList<>();
    }

    /**
     * Adds a row to the table
     *
     * @deprecated will be removed in future version!
     */
    @Deprecated
    public void addRow(String[] data) {
        // Check if values is not null
        if (data == null)
            throw new NullPointerException();

        // Copy the values to the array
        String[] copied = new String[columns];
        for (int i = 0; i < columns; i++) {
            copied[i] = data.length > i && data[i] != null ? data[i] : "";
            if (copied[i].length() > maxSize[i])
                maxSize[i] = copied[i].length();
        }

        // Add to list
        values.add(copied);
    }

    /**
     * Returns the table as a string The string ends with a newline
     */
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (String[] row : values) {
            for (int i = 0; i < columns - 1; i++) {
                builder.append(row[i]);
                int padding = maxSize[i] - row[i].length() + 2;
                builder.append(" ".repeat(Math.max(0, padding)));
            }
            builder.append(row[columns - 1]);
            builder.append('\n');
        }

        return builder.toString();
    }
}