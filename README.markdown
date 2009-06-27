## Hyperic HQ Dynomite plugin

This project is a Hyperic HQ plugin for monitoring [Dynomite](http://wiki.github.com/cliffmoon/dynomite)

Source code is available at [github.com/hyperic/hq-dynomite](http://github.com/hyperic/hq-dynomite)

Plugin binary is available at [hudson.hyperic.com/job/hq-dynomite-plugin](http://hudson.hyperic.com/job/hq-dynomite-plugin/)

More info at [HyperForge/Dynomite](http://support.hyperic.com/display/hypcomm/Dynomite)

### Auto-Discovery

The Dynomite server process is auto-discovered and monitoring properties are configured
using the config file.  There are no service types defined yet.

### Metrics

Server process metrics are collectd via SIGAR.

Metrics are also collected from */rpc/rates/$instance* - not sure if those are working atm,
(commented out in *dynomite/web/index.html*) - ideas?

### Log File Tracking

None yet.

### Config File Tracking

None yet.

### Control Actions

None yet.

### Dependencies

[jackson](http://jackson.codehaus.org/) - see LICENSES file for details.

### Dynomite version support

Tested on Linux using [Dynomite 3203e15](http://github.com/cliffmoon/dynomite/tree/master)

### Hyperic HQ version support

Tested with [Hyperic HQ](http://www.hyperic.com/) version 4.1.2

