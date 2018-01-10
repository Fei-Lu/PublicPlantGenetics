/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package format.table;

import java.util.HashMap;
import java.util.List;

/**
 *
 * @author feilu
 * @param <T>
 */
public abstract class TableAbstract<T> implements TableInterface<T> {
    protected List<String> header = null;
    protected List<List<T>> cells = null;
    protected HashMap<String, Integer> hiMap = new HashMap<>();
    protected int sortColumnIndex  = -1;
    
    protected void buildHIMap () {
        for (int i = 0; i < header.size(); i++) {
            hiMap.put(header.get(i), i);
        }
    }
    
    @Override
    public List<String> getHeader() {
        return this.header;
    }
    
    @Override
    public String getHeaderName (int columnIndex) {
        return header.get(columnIndex);
    }
    
    @Override
    public String getCellAsString (int rowIndex, int columnIndex) {
        return this.getCell(rowIndex, columnIndex).toString();
    }
    
    @Override
    public Double getCellAsDouble (int rowIndex, int columnIndex) {
        T ob = this.getCell(rowIndex, columnIndex);
        if (ob instanceof Number) {
            return ((Number) ob).doubleValue();
        }
        else {
            return null;
        }
    }
    
    @Override
    public Integer getCellAsInteger (int rowIndex, int columnIndex) {
        T ob = this.getCell(rowIndex, columnIndex);
        if (ob instanceof Number) {
            return ((Number) ob).intValue();
        }
        else {
            return null;
        }
    }
    
    @Override
    public int getColumnNumber () {
        return this.header.size();
    }
    
    @Override
    public int getColumnIndex(String columnName) {
        return hiMap.get(columnName);
    }
    
    @Override
    public void sortAsText(String columnName) {
        int columnIndex = this.hiMap.get(columnName);
        this.sortAsText(columnIndex);
    }

    @Override
    public boolean sortAsNumber (String columnName) {
        int columnIndex = this.hiMap.get(columnName);
        return this.sortAsNumber(columnIndex);
    }
    
    @Override
    public void removeColumn(String columnName) {
        int columnIndex = this.hiMap.get(columnName);
        this.removeColumn(columnIndex);
    }
}
