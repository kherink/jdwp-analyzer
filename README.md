jdwp-analyzer
=============

JDWP (Java Debug Wire Protocol) analysis tool to help you debug your JVM or debugger.

It sits between a debugger and a JVM as a TCP proxy, forwards every packet unchanged, and decodes each one
against the complete JDWP specification as of JDK 26: all 18 command sets and 95 commands, every event kind
and every event-request modifier (including `PlatformThreadsOnly`). IDs are resolved to names as the session
goes on: classes, methods, fields, threads, thread groups, strings, modules, frames, source lines and event
requests.

## Running

Requires JDK 21+ (Gradle provisions one through the toolchain if needed).

```
./gradlew :app:run
```

opens the setup screen. The original command-line arguments still work:

```
./gradlew :app:run --args="<inPort> <outAddress> <reqDelay> <respDelay> [<log_dir>]"   # proxy
./gradlew :app:run --args="<log_dir>"                                                  # replay a log
```

- `inPort`: the port your debugger attaches to
- `outAddress`: `host:port` where the JVM listens, e.g. one started with
  `-agentlib:jdwp=transport=dt_socket,server=y,address=5005`; a localhost address can be just a port
- `reqDelay` / `respDelay`: milliseconds to wait after forwarding each packet to the VM / to the debugger
- `log_dir`: where to write `seq.log`, the raw packet log. The format is unchanged, so logs from the old Java
  version replay too.

`./gradlew :app:packageDistributionForCurrentOS` builds a native installer.

## Modules

- `core`: the packet model, the declarative protocol spec (`decode/JdwpSpec.kt`), the decoder, the registry of
  what has been learnt about the debuggee, the proxy, and packet logs. No UI dependencies.
- `app`: the Compose Desktop UI. It shows a packet table with filters, decoded command and reply trees, a hex
  view that highlights the selected field, and an inspector for any ID.

## Tests

`./gradlew :core:test` runs the unit tests and two end-to-end tests against a real JVM:

- **`EndToEndTest`** drives a debuggee through the proxy with JDI: breakpoints, stepping, watchpoints,
  exceptions, monitor events, method invocation, class redefinition, and more.
- **`RawClientTest`** sends the commands JDI never uses straight over the wire.

Every packet must decode completely and every command must be answered.
