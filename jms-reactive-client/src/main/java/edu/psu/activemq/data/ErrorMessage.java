package edu.psu.activemq.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Data;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
public class ErrorMessage {
  String shortDescription;
  String sourceSystem;
  String description;
  String stack;
}
