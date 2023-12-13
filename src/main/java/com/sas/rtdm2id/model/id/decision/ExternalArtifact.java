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
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * ExternalArtifact
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaClientCodegen", date = "2020-11-06T21:19:27.553+03:00")


public class ExternalArtifact {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("artifactType")
  private String artifactType = null;

  @JsonProperty("artifactProperties")
  private Object artifactProperties = null;

  @JsonProperty("parentURI")
  private String parentURI = null;

  @JsonProperty("version")
  private Integer version = null;

  public ExternalArtifact name(String name) {
    this.name = name;
    return this;
  }

  /**
   * An assigned name of the artifact.
   *
   * @return name
   **/
  @ApiModelProperty(value = "An assigned name of the artifact.")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ExternalArtifact artifactType(String artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  /**
   * The type of artifact.
   *
   * @return artifactType
   **/
  @ApiModelProperty(value = "The type of artifact.")
  public String getArtifactType() {
    return artifactType;
  }

  public void setArtifactType(String artifactType) {
    this.artifactType = artifactType;
  }

  public ExternalArtifact artifactProperties(Object artifactProperties) {
    this.artifactProperties = artifactProperties;
    return this;
  }

  /**
   * A map of keys to strings that are relevant for this artifact type. For example, for an analytic store, this map has a key for the &#x60;astoreUri&#x60;.
   *
   * @return artifactProperties
   **/
  @ApiModelProperty(value = "A map of keys to strings that are relevant for this artifact type. For example, for an analytic store, this map has a key for the `astoreUri`.")
  public Object getArtifactProperties() {
    return artifactProperties;
  }

  public void setArtifactProperties(Object artifactProperties) {
    this.artifactProperties = artifactProperties;
  }

  public ExternalArtifact parentURI(String parentURI) {
    this.parentURI = parentURI;
    return this;
  }

  /**
   * The URI of the object, such as the model or business rule set, that makes use of this artifact.
   *
   * @return parentURI
   **/
  @ApiModelProperty(value = "The URI of the object, such as the model or business rule set, that makes use of this artifact.")
  public String getParentURI() {
    return parentURI;
  }

  public void setParentURI(String parentURI) {
    this.parentURI = parentURI;
  }

  public ExternalArtifact version(Integer version) {
    this.version = version;
    return this;
  }

  /**
   * This media type&#39;s schema version number. This representation is version 1.
   *
   * @return version
   **/
  @ApiModelProperty(value = "This media type's schema version number. This representation is version 1.")
  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ExternalArtifact externalArtifact = (ExternalArtifact) o;
    return Objects.equals(this.name, externalArtifact.name) &&
            Objects.equals(this.artifactType, externalArtifact.artifactType) &&
            Objects.equals(this.artifactProperties, externalArtifact.artifactProperties) &&
            Objects.equals(this.parentURI, externalArtifact.parentURI) &&
            Objects.equals(this.version, externalArtifact.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, artifactType, artifactProperties, parentURI, version);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalArtifact {\n");

    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    artifactProperties: ").append(toIndentedString(artifactProperties)).append("\n");
    sb.append("    parentURI: ").append(toIndentedString(parentURI)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
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
