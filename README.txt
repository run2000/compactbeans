Overview
--------

This is a separated-out implementation of the java.beans.Introspector and
associated Descriptor classes, targeted at Java 1.8 compact profile 1.

The code is based on the openjdk7u40 source bundle.

License
-------

The license is the same as for openjdk7 itself. That is, GPL version 2 only,
with the "Classpath" exception as described in the LICENSE file.

Design goals
------------

Able to keep consistent with the openjdk implementation and future releases
of openjdk.

API compatibility as much as possible. In the simplest case, a package
import rename is all that is required.

Stripped down code base to essentials for smaller footprint, as consistent
with compact profile environments.

Drop in to many server-side situations where bean-like properties and events
are used. Examples:
  * JSP/EL
  * Velocity
  * Log4j

Working assumptions
-------------------

Code that pre-supposes a GUI or an IDE is omitted.

  * No reference to the java.awt.* packages.
     - No icons

Anything that requires a specific reference to the java.beans.* package
is omitted.

  * No BeanInfo classes, beyond the GenericBeanInfo returned by the
    introspector.
     - due to java.beans.BeanInfo dependency
  * No Property editors
     - due to java.beans.PropertyEditor dependency.
  * No introspection of explicit bean info.
     - due to java.beans.BeanInfo dependency
  * Any feature that requires explicit bean info to be exposed is omitted.
     - no ParameterDescriptor
  * No PropertyChangeEvents
     - due to java.beans.PropertyChangeEvent and java.beans.PropertyChangeListener
       dependency
  * No VetoableChangeListeners
     - due to java.beans.VetoableChangeListener dependency
     - no constrained properties
     - can still listen for other events
  * Remove constructors and methods that are not required by the introspector
     - Descriptors appear immutable from outside the java.beans.* package

No XMLEncoder, XMLDecoder
  * Reference to compact profile 2 stuff, namely SAX
  * Lots of additional code for marginal gain

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
