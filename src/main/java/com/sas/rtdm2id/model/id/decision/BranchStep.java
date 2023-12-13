/*
Copyright © 2023, SAS Institute Inc., Cary, NC, USA.  All Rights Reserved.
SPDX-License-Identifier: Apache-2.0
*/
/*
 * Decisions
 * The Decisions API supports the life cycle of decision data.
 *
 * OpenAPI spec version: 7
 *
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */


package com.sas.rtdm2id.model.id.decision;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a branch decision flow step.  A branch step contains a list of branchCases and a defaultCase.  During the execution of a branch step each branchCases condition is evaluated in order.  The decision flow will follow the first branchCases who&#39;s condition evaluates true.  If no branchCases&#39;s condition evaluates true the branchStep&#39;s defaultCase path is followed.
 */
@ApiModel(description = "Represents a branch decision flow step.  A branch step contains a list of branchCases and a defaultCase.  During the execution of a branch step each branchCases condition is evaluated in order.  The decision flow will follow the first branchCases who's condition evaluates true.  If no branchCases's condition evaluates true the branchStep's defaultCase path is followed.")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-11-06T21:19:27.553+03:00")


public class BranchStep {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("branchType")
  private String branchType = null;

  @JsonProperty("branchCases")
  private List<BranchCase> branchCases = null;

  @JsonProperty("defaultCase")
  private ConditionBranch defaultCase = null;

  public BranchStep name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Logical name of this node for business user identification.
   *
   * @return name
   **/
  @ApiModelProperty(value = "Logical name of this node for business user identification.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public BranchStep branchType(String branchType) {
    this.branchType = branchType;
    return this;
  }

  /**
   * One of: * &#x60;range&#x60; - A branch step with branchType \&quot;range\&quot; contains branchCases that define a \&quot;range\&quot; condition for either a variable lower bound, a variable upper bound, or a variable range. * &#x60;equals&#x60; - A branch step with branchType \&quot;equals\&quot; contains branchCases that define \&quot;equals\&quot; conditions.  All conditions in a \&quot;equals\&quot; branch must use the \&quot;&#x3D;\&quot; operator.  Several \&quot;equals\&quot; conditions may be \&quot;OR\&quot;ed. * &#x60;like&#x60; -  A branch step with branchType \&quot;like\&quot; contains branchCases that define \&quot;like\&quot; conditions.   All conditions in a \&quot;equals\&quot; branch must use the \&quot;like\&quot; operator.  Several \&quot;like\&quot; conditions may be \&quot;OR\&quot;ed.
   *
   * @return branchType
   **/
  @ApiModelProperty(required = true, value = "One of: * `range` - A branch step with branchType \"range\" contains branchCases that define a \"range\" condition for either a variable lower bound, a variable upper bound, or a variable range. * `equals` - A branch step with branchType \"equals\" contains branchCases that define \"equals\" conditions.  All conditions in a \"equals\" branch must use the \"=\" operator.  Several \"equals\" conditions may be \"OR\"ed. * `like` -  A branch step with branchType \"like\" contains branchCases that define \"like\" conditions.   All conditions in a \"equals\" branch must use the \"like\" operator.  Several \"like\" conditions may be \"OR\"ed. ")
  public String getBranchType() {
    return branchType;
  }

  public void setBranchType(String branchType) {
    this.branchType = branchType;
  }

  public BranchStep branchCases(List<BranchCase> branchCases) {
    this.branchCases = branchCases;
    return this;
  }

  public BranchStep addBranchCasesItem(BranchCase branchCasesItem) {
    if (this.branchCases == null) {
      this.branchCases = new ArrayList<>();
    }
    this.branchCases.add(branchCasesItem);
    return this;
  }

  /**
   * Specifies an array of branchCases.  At execution time each branchCase is evaulated in order.  The \&quot;onTrue\&quot; path of the first branchCase whose condition(s) evaluates true is followed.  If no branchCase&#39;s condition(s) evaluates true the defaultCase path is followed.
   *
   * @return branchCases
   **/
  @ApiModelProperty(value = "Specifies an array of branchCases.  At execution time each branchCase is evaulated in order.  The \"onTrue\" path of the first branchCase whose condition(s) evaluates true is followed.  If no branchCase's condition(s) evaluates true the defaultCase path is followed.")
  public List<BranchCase> getBranchCases() {
    return branchCases;
  }

  public void setBranchCases(List<BranchCase> branchCases) {
    this.branchCases = branchCases;
  }

  public BranchStep defaultCase(ConditionBranch defaultCase) {
    this.defaultCase = defaultCase;
    return this;
  }

  /**
   * Specifies the steps to execute if no branchCase&#39;s condition(s) evaluate true.
   *
   * @return defaultCase
   **/
  @ApiModelProperty(value = "Specifies the steps to execute if no branchCase's condition(s) evaluate true.")
  public ConditionBranch getDefaultCase() {
    return defaultCase;
  }

  public void setDefaultCase(ConditionBranch defaultCase) {
    this.defaultCase = defaultCase;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BranchStep branchStep = (BranchStep) o;
    return Objects.equals(this.name, branchStep.name) &&
            Objects.equals(this.branchType, branchStep.branchType) &&
            Objects.equals(this.branchCases, branchStep.branchCases) &&
            Objects.equals(this.defaultCase, branchStep.defaultCase);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, branchType, branchCases, defaultCase);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BranchStep {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    branchType: ").append(toIndentedString(branchType)).append("\n");
    sb.append("    branchCases: ").append(toIndentedString(branchCases)).append("\n");
    sb.append("    defaultCase: ").append(toIndentedString(defaultCase)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }

}

