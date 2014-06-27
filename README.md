Overview
========

This is a separated-out implementation of the java.beans.Introspector and
associated Descriptor classes, targeted at Java 1.8SE Compact Profile 1.
The intended platform is a small server-like device capable of running
JavaSE embedded, such as a Raspberry Pi.

The code is based on the openjdk8u20-dev source bundle, downloaded from the
Mercurial repository at http://hg.openjdk.java.net/jdk8u/jdk8u20-dev

License
-------

The license is the same as for OpenJDK8 itself. That is, GPL version 2 only,
with the "Classpath" exception as described in the LICENSE file.

This can be found on the OpenJDK web site at
http://openjdk.java.net/legal/gplv2+ce.html

Design goals
------------

Track and keep consistent with the OpenJDK implementation and future releases
of OpenJDK.

API compatibility as much as possible. In the simplest case, a package
import rename is all that is required.

Stripped down code base to runtime only for smaller footprint, as consistent
with compact profile environments.

Try and deal with mutability concerns in the FeatureDescriptor classes, so that
any mutation of descriptor data is isolated from objects in the BeanInfo
cache.

Drop in to many server-side situations where bean-like properties and events
are used. Examples:

  * JSP
  * Log4j

Working assumptions
-------------------

Code that pre-supposes a GUI or an IDE is omitted.

  * No reference to the java.applet.* packages.
     - No bean applet instantiation
  * No reference to the java.awt.* packages.
     - No icons

No classes intended for design-time GUIs.

  * Customizer interface omitted
  * EventHandler omitted
  * PropertyEditor interface omitted
     - similarly PropertyEditorManager, PropertyEditorSupport omitted
  * Visibility interface omitted

Beans static class is included, in abbreviated form.

  * No reference to BeanContext or AppletInitializer, so no overloads of
    instantiate() method.
  * No implementation of "guiAccessible" or "designTime" properties.
     - getters for each are hard-coded to false

No Long-Term Persistence framework.

  * No Encoder, PersistenceDelegate, XMLEncoder, XMLDecoder
     - reference to compact profile 2 stuff, namely SAX
     - no DefaultPersistenceDelegate class
  * No ExceptionListener interface
  * No Expression, Statement classes
  * No ConstructorProperties annotation
  * No Transient annotation

Address mutability concerns:

  * Remove or reduce visibility of setter methods that are not required
    by the introspector
     - features appear immutable from outside the beans.* package
  * Add FeatureBuilder classes for creating new descriptors from scratch
     - a safe area for mutability before building the final immutable
       descriptor object
     - copy on write style encouraged outside the beans.* package

Property Change Events
----------------------

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

Vetoable Changes
----------------

These are included in the source, including:

  * PropertyVetoException
  * VetoableChangeListener
  * VetoableChangeListenerProxy
  * VetoableChangeSupport

Recompiling the source of any implementation classes will be necessary
to take advantage of these classes and interfaces.

Other assumptions
-----------------

Classes finalized or package-internal where possible. Similarly, mutating
methods inside Descriptor classes are now package-internal or private where
possible.

Rely less heavily on inheritance, where possible.

  * FeatureDescriptor made an interface.
  * Added a DescriptorData class to all the FeatureDescriptor implementations
     - contains mutable information previously held directly in
       FeatureDescriptor
  * Added a DescriptorType to FeatureDescriptor for each feature descriptor
    type.
     - this avoids requiring instanceof tests to distinguish descriptors
       such as PropertyDescriptor and IndexedPropertyDescriptor.
  * Similarly, added an isIndexed() method to PropertyChangeEvent and
    IndexedPropertyChangeEvent.
