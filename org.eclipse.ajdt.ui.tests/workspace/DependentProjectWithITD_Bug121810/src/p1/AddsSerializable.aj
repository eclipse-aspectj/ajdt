package p1;

import org.aspectj.lang.annotation.SuppressAjWarnings;

@SuppressAjWarnings
public aspect AddsSerializable {
  declare parents : p2.ToBeSerializable implements java.io.Serializable;
}
