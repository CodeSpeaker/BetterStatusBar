package betterstatusbar.status.util;

import com.intellij.uiDesigner.core.GridConstraints;

public class GridConstraintsUtil {

    public static GridConstraints getPositionGridConstraints(int row, int column) {
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setRow(row);
        gridConstraints.setColumn(column);
        return gridConstraints;
    }

    public static GridConstraints getPositionGridConstraints(int row, int column, int colSpan, int rowSpan) {
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setRow(row);
        gridConstraints.setColumn(column);

        if (colSpan != -1) {
            gridConstraints.setColSpan(colSpan);
        }

        if (rowSpan != -1) {
            gridConstraints.setRowSpan(rowSpan);
        }
        return gridConstraints;
    }

    public static GridConstraints getPositionGridConstraints(int row, int column, int colSpan, int width, int height) {
        GridConstraints gridConstraints = new GridConstraints();
        gridConstraints.setRow(row);
        gridConstraints.setColumn(column);
        gridConstraints.myMinimumSize.setSize(width, height);

        if (colSpan != -1) {
            gridConstraints.setColSpan(colSpan);
        }
        return gridConstraints;
    }
}
