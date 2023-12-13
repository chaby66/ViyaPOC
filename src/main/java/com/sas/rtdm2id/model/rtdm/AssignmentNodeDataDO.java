/*
Copyright © 2023, SAS Institute Inc., Cary, NC, USA.  All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
package com.sas.rtdm2id.model.rtdm;

import com.sas.rtdm2id.model.rtdm.extension.*;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "archivedPublishState",
        "assignments",
        "codeChangeUnpublished",
        "codeEverBeenPublished",
        "controlKey",
        "dateModified",
        "droppedCount",
        "excludeFlag",
        "excludeIconPath",
        "firstOccurrenceToPublish",
        "iconPath",
        "inputNodes",
        "label",
        "loadedFromOldDavLocation",
        "loading",
        "needToPersist",
        "nodeId",
        "nodeName",
        "nodeState",
        "nodeTags",
        "nodeType",
        "oldVersionNumber",
        "outputNodes",
        "persisting",
        "publishState",
        "refreshNodeDataDO",
        "sortByList",
        "splitters",
        "subjectID",
        "systemExtraValues",
        "totalCount",
        "validInputSubject",
        "validOutputSubject",
        "versionNumber",
        "x",
        "y",
        "objid"
})
public class AssignmentNodeDataDO implements Serializable {
    private static final long serialVersionUID = -1L;

    @XmlElement(name = "ArchivedPublishState")
    protected byte archivedPublishState;
    @XmlElement(name = "Assignments")
    protected Assignments assignments;
    @XmlElement(name = "CodeChangeUnpublished", required = true)
    protected String codeChangeUnpublished;
    @XmlElement(name = "CodeEverBeenPublished", required = true)
    protected String codeEverBeenPublished;
    @XmlElement(name = "ControlKey")
    protected byte controlKey;
    @XmlElement(name = "DateModified", required = true)
    protected String dateModified;
    @XmlElement(name = "DroppedCount")
    protected byte droppedCount;
    @XmlElement(name = "ExcludeFlag", required = true)
    protected String excludeFlag;
    @XmlElement(name = "ExcludeIconPath", required = true)
    protected String excludeIconPath;
    @XmlElement(name = "FirstOccurrenceToPublish")
    protected byte firstOccurrenceToPublish;
    @XmlElement(name = "IconPath", required = true)
    protected String iconPath;
    @XmlElement(name = "InputNodes", required = true)
    protected InputNodes inputNodes;
    @XmlElement(name = "Label", required = true)
    protected String label;
    @XmlElement(name = "LoadedFromOldDavLocation", required = true)
    protected String loadedFromOldDavLocation;
    @XmlElement(name = "Loading", required = true)
    protected String loading;
    @XmlElement(name = "NeedToPersist", required = true)
    protected String needToPersist;
    @XmlElement(name = "NodeId", required = true)
    protected String nodeId;
    @XmlElement(name = "NodeName", required = true)
    protected String nodeName;
    @XmlElement(name = "NodeState")
    protected byte nodeState;
    @XmlElement(name = "NodeTags", required = true)
    protected NodeTags nodeTags;
    @XmlElement(name = "NodeType")
    protected byte nodeType;
    @XmlElement(name = "OldVersionNumber", required = true)
    protected String oldVersionNumber;
    @XmlElement(name = "OutputNodes", required = true)
    protected OutputNodes outputNodes;
    @XmlElement(name = "Persisting", required = true)
    protected String persisting;
    @XmlElement(name = "PublishState")
    protected byte publishState;
    @XmlElement(name = "RefreshNodeDataDO")
    protected String refreshNodeDataDO;
    @XmlElement(name = "SortByList", required = true)
    protected SortByList sortByList;
    @XmlElement(name = "Splitters", required = true)
    protected Splitters splitters;
    @XmlElement(name = "SubjectID", required = true)
    protected String subjectID;
    @XmlElement(name = "SystemExtraValues", required = true)
    protected String systemExtraValues;
    @XmlElement(name = "TotalCount")
    protected byte totalCount;
    @XmlElement(name = "ValidInputSubject", required = true)
    protected String validInputSubject;
    @XmlElement(name = "ValidOutputSubject", required = true)
    protected String validOutputSubject;
    @XmlElement(name = "VersionNumber")
    protected float versionNumber;
    @XmlElement(name = "X")
    protected short x;
    @XmlElement(name = "Y")
    protected short y;
    @XmlAttribute(name = "objid")
    protected Short objid;

    public byte getArchivedPublishState() {
        return archivedPublishState;
    }

    public void setArchivedPublishState(byte archivedPublishState) {
        this.archivedPublishState = archivedPublishState;
    }

    public Assignments getAssignments() {
        return assignments;
    }

    public void setAssignments(Assignments assignmentVariables) {
        this.assignments = assignmentVariables;
    }

    public String getCodeChangeUnpublished() {
        return codeChangeUnpublished;
    }

    public void setCodeChangeUnpublished(String codeChangeUnpublished) {
        this.codeChangeUnpublished = codeChangeUnpublished;
    }

    public String getCodeEverBeenPublished() {
        return codeEverBeenPublished;
    }

    public void setCodeEverBeenPublished(String codeEverBeenPublished) {
        this.codeEverBeenPublished = codeEverBeenPublished;
    }

    public byte getControlKey() {
        return controlKey;
    }

    public void setControlKey(byte controlKey) {
        this.controlKey = controlKey;
    }

    public String getDateModified() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified = dateModified;
    }

    public byte getDroppedCount() {
        return droppedCount;
    }

    public void setDroppedCount(byte droppedCount) {
        this.droppedCount = droppedCount;
    }

    public String getExcludeFlag() {
        return excludeFlag;
    }

    public void setExcludeFlag(String excludeFlag) {
        this.excludeFlag = excludeFlag;
    }

    public String getExcludeIconPath() {
        return excludeIconPath;
    }

    public void setExcludeIconPath(String excludeIconPath) {
        this.excludeIconPath = excludeIconPath;
    }

    public byte getFirstOccurrenceToPublish() {
        return firstOccurrenceToPublish;
    }

    public void setFirstOccurrenceToPublish(byte firstOccurrenceToPublish) {
        this.firstOccurrenceToPublish = firstOccurrenceToPublish;
    }

    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public InputNodes getInputNodes() {
        return inputNodes;
    }

    public void setInputNodes(InputNodes inputNodes) {
        this.inputNodes = inputNodes;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLoadedFromOldDavLocation() {
        return loadedFromOldDavLocation;
    }

    public void setLoadedFromOldDavLocation(String loadedFromOldDavLocation) {
        this.loadedFromOldDavLocation = loadedFromOldDavLocation;
    }

    public String getLoading() {
        return loading;
    }

    public void setLoading(String loading) {
        this.loading = loading;
    }

    public String getNeedToPersist() {
        return needToPersist;
    }

    public void setNeedToPersist(String needToPersist) {
        this.needToPersist = needToPersist;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public byte getNodeState() {
        return nodeState;
    }

    public void setNodeState(byte nodeState) {
        this.nodeState = nodeState;
    }

    public NodeTags getNodeTags() {
        return nodeTags;
    }

    public void setNodeTags(NodeTags nodeTags) {
        this.nodeTags = nodeTags;
    }

    public byte getNodeType() {
        return nodeType;
    }

    public void setNodeType(byte nodeType) {
        this.nodeType = nodeType;
    }

    public String getOldVersionNumber() {
        return oldVersionNumber;
    }

    public void setOldVersionNumber(String oldVersionNumber) {
        this.oldVersionNumber = oldVersionNumber;
    }

    public OutputNodes getOutputNodes() {
        return outputNodes;
    }

    public void setOutputNodes(OutputNodes outputNodes) {
        this.outputNodes = outputNodes;
    }

    public String getPersisting() {
        return persisting;
    }

    public void setPersisting(String persisting) {
        this.persisting = persisting;
    }

    public byte getPublishState() {
        return publishState;
    }

    public void setPublishState(byte publishState) {
        this.publishState = publishState;
    }

    public String getRefreshNodeDataDO() {
        return refreshNodeDataDO;
    }

    public void setRefreshNodeDataDO(String refreshNodeDataDO) {
        this.refreshNodeDataDO = refreshNodeDataDO;
    }

    public SortByList getSortByList() {
        return sortByList;
    }

    public void setSortByList(SortByList sortByList) {
        this.sortByList = sortByList;
    }

    public Splitters getSplitters() {
        return splitters;
    }

    public void setSplitters(Splitters splitters) {
        this.splitters = splitters;
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectID) {
        this.subjectID = subjectID;
    }

    public String getSystemExtraValues() {
        return systemExtraValues;
    }

    public void setSystemExtraValues(String systemExtraValues) {
        this.systemExtraValues = systemExtraValues;
    }

    public byte getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(byte totalCount) {
        this.totalCount = totalCount;
    }

    public String getValidInputSubject() {
        return validInputSubject;
    }

    public void setValidInputSubject(String validInputSubject) {
        this.validInputSubject = validInputSubject;
    }

    public String getValidOutputSubject() {
        return validOutputSubject;
    }

    public void setValidOutputSubject(String validOutputSubject) {
        this.validOutputSubject = validOutputSubject;
    }

    public float getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(float versionNumber) {
        this.versionNumber = versionNumber;
    }

    public short getX() {
        return x;
    }

    public void setX(short x) {
        this.x = x;
    }

    public short getY() {
        return y;
    }

    public void setY(short y) {
        this.y = y;
    }

    public Short getObjid() {
        return objid;
    }

    public void setObjid(Short objid) {
        this.objid = objid;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "ibVariableDOs"
    })
    public static class Assignments implements Serializable {
        private static final long serialVersionUID = -1L;

        @XmlElement(name = "IBVariableDO")
        protected List<IBVariableDO> ibVariableDOs;

        public List<IBVariableDO> getIBVariableDOs() {
            if (ibVariableDOs == null) {
                ibVariableDOs = new ArrayList<>();
            }
            return this.ibVariableDOs;
        }

    }
}
