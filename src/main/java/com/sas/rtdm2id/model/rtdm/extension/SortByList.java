/*
Copyright © 2023, SAS Institute Inc., Cary, NC, USA.  All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.sas.rtdm2id.model.rtdm.extension;

import javax.xml.bind.annotation.*;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "value"
})
public class SortByList implements Serializable {
    private static final long serialVersionUID = -1L;
    
    @XmlValue
    protected String value;
    @XmlAttribute(name = "EmptyList")
    protected String emptyList;
       
    public String getValue() {
        return value;
    }
       
    public void setValue(String value) {
        this.value = value;
    }
       
    public String getEmptyList() {
        return emptyList;
    }
       
    public void setEmptyList(String value) {
        this.emptyList = value;
    }

}
