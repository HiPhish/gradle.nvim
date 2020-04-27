.. default-role:: code

########################
 Hacking on gradle.nvim
########################


High-level overview of the project
##################################

Neovim allows us to implement remote plugins, which are separate processes
communicating with a Neovim instance. Gradle provides us with a Java library
API. To connect Neovim and Gradle we need to write a Java remote plugin which
acts as a mediator between the two.

The following illustration gives an overview of how the individual parts
connect. The communication protocol is on top of the arrows, the underlying
transport mechanism is beneath the arrows.

.. code-block::

   +--------+    MsgPack RPC    +-------------+    Tooling API     +--------+
   | Neovim | <---------------> | Gradle.nvim | <----------------> | Gradle |
   +--------+    Standard IO    +-------------+    Java library    +--------+

The remote plugin is a Java application and needs to be compiled before the
plugin can be run. We use of course Gradle as the build system.


Bootstrapping process
#####################

Bootstrapping is the act of connecting the components at runtime. We first
source a Vim script file in order to set up the user-side interface, i.e. Vim
commands, functions, variables and similar. Then a new job is started which
launches the compiled remote plugin. The Vim-side communicates over standard
input and -output with the remote plugin via RPC.


Further reading
###############

To read the offline version of a manual replace the leading part of the URL
(`https://docs.gradle.org/current/`) with the local URL to the documentation
directory. The exact location depends on where you have installed Gradle on
your system, e.g. `file:///home/johndoe/.java-packages/gradle-6.2.1/docs/`.

- `Gradle Tooling API guide <https://docs.gradle.org/current/userguide/third_party_integration.html#embedding>`
- `Gradle Tooling API reference <https://docs.gradle.org/current/javadoc/org/gradle/tooling/package-summary.html>`
