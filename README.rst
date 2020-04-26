.. default-role:: code

###############################
 Gradle integration for Neovim
###############################

This plugin aims to provide fist-class integration of the `Gradle`_ build
system with the `Neovim`_ text editor. For now nothing works though.

Installation
############

This is a remote plugin with a compiled component. It requires at least JDK 11.
Please see the INSTALL_ file for more details.

1) Install this plugin like any other Vim plugin
2) Install the correct Gradle wrapper
   .. code-block:: sh
      gradle wrapper
2) Build the Java application according to the below instructions
   .. code-block:: sh
      ./gradlew install

When running the compiled component, communication happens over standard input
and standard output (only for testing purpose):

.. code-block:: sh

   ./build/install/gradle.nvim/bin/gradle.nvim


Usage
#####

Source the bootstrap file, then execute one of the commands it defines to play
around with:

.. code-block:: vim

   source plugin/gradle.vim

   " Echo an OK
   GradleHanshake
   " Raise an error
   GradleThrow
   " Display a list of Gradle tasks
   GradleTasks


Status
######

Currently utterly useless, but the foundations are in place. I will be slowly
chipping away at it, adding features. I still need to decide on what interface
to even provide, then actually implement it.


License
#######

Released under the MIT (Expat) license, please see the `COPYING`_ file for
details.


.. ----------------------------------------------------------------------------
.. _Gradle: https://gradle.org/
.. _Neovim: https://neovim.io/
.. _INSTALL: INSTALL.rst
.. _COPYING: COPYING.txt
