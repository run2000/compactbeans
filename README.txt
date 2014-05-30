Overview
--------

This is a separated-out implementation of the java.beans.Introspector and
associated Descriptor classes, targeted at Java 1.8 compact profile 1.

The code is based on the openjdk7u40 source bundle, downloaded from
https://jdk7.java.net/source.html

License
-------

The license is the same as for openjdk7 itself. That is, GPL version 2 only,
with the "Classpath" exception as described in the LICENSE file.

This can be found on the openjdk web site at
http://openjdk.java.net/legal/gplv2+ce.html

Design goals
------------

Track and keep consistent with the openjdk implementation and future releases
of openjdk.

API compatibility as much as possible. In the simplest case, a package
import rename is all that is required.

Stripped down code base to essentials for smaller footprint, as consistent
with compact profile environments.

Drop in to many server-side situations where bean-like properties and events
are used. Examples:
  * JSP
  * Log4j

As it turns out, many of these libraries also use properties and property
change events internally, so it becomes useful to have the property
change event infrastructure for these situations.

Working assumptions
-------------------

Code that pre-supposes a GUI or an IDE is omitted.

  * No reference to the java.applet.* packages.
     - No bean applet instantiation
  * No reference to the java.awt.* packages.
     - No icons

Most things(*) that requires a specific reference to the java.beans.* package
are omitted.

  * No BeanInfo classes, beyond the GenericBeanInfo returned by the
    introspector.
     - due to java.beans.BeanInfo dependency
  * No Property editors
     - due to java.beans.PropertyEditor dependency.
  * No introspection of explicit bean info.
     - due to java.beans.BeanInfo dependency
  * Any feature that requires explicit bean info to be exposed is omitted.
     - no ParameterDescriptor
  * No VetoableChangeListeners
     - due to java.beans.VetoableChangeListener dependency
     - no constrained properties
     - can still listen for other events
  * Remove constructors and methods that are not required by the introspector
     - descriptors appear immutable from outside the java.beans.* package

Beans static class is included, in abbreviated form.
  * No reference to BeanContext or AppletInitializer, so no overloads of
    instantiate() method.
  * No implementation of "guiAccessible" or "designTime" properties.

No classes intended for design-time GUIs.
  * EventHandler omitted
  * Visibility interface omitted

No XMLEncoder, XMLDecoder
  * Reference to compact profile 2 stuff, namely SAX
  * Unused in examples encountered so far


(*)Property Change Events
-------------------------
These are included in the source, including:

  * IndexedPropertyChangeEvent
  * PropertyChangeEvent
  * PropertyChangeListener
  * PropertyChangeListenerProxy
  * PropertyChangeSupport

Recompiling the source of any implementation classes will be necessary
to take advantage of these classes and interfaces.

As it turns out, many libraries use properties and property change events
internally, so it becomes useful to have the property change event
infrastructure for these situations.

Other assumptions
-----------------

Classes finalized or package-internal where possible. Similarly, mutating
methods inside Descriptor classes are now package-internal or private where
possible.

Rely less heavily on inheritance, where possible.
  * FeatureDescriptor made an interface.
  * Added a DescriptorType to FeatureDescriptor for each feature descriptor
    type.
     - The avoids requiring instanceof tests to distinguish descriptors
       such as PropertyDescriptor and IndexedPropertyDescriptor.
     - This is the only code that has been added.
  * Similarly, added an isIndexed() method to PropertyChangeEvent and
    IndexedPropertyChangeEvent.
