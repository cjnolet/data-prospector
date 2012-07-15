package sonixbp.domain;

import sonixbp.TripleValueType;

import java.util.ArrayList;
import java.util.List;

public class TripleIndexDescription {

    private String index;
    private TripleValueType type;
    private String dataType;

    public TripleIndexDescription(String index, TripleValueType type, String dataType) {
        this.index = index;
        this.type = type;
        this.dataType = dataType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public TripleValueType getType() {
        return type;
    }

    public void setType(TripleValueType type) {
        this.type = type;
    }
}
