/*
 * NOTE: This copyright does *not* cover user programs that use HQ
 * program services by normal system calls through the application
 * program interfaces provided as part of the Hyperic Plug-in Development
 * Kit or the Hyperic Client Development Kit - this is merely considered
 * normal use of the program, and does *not* fall under the heading of
 * "derived work".
 * 
 * Copyright (C) [2009], Hyperic, Inc.
 * This file is part of HQ.
 * 
 * HQ is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.hq.plugin.dynomite;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.hyperic.hq.product.Collector;
import org.hyperic.hq.product.ConfigFileTrackPlugin;
import org.hyperic.hq.product.DaemonDetector;
import org.hyperic.hq.product.PluginException;
import org.hyperic.hq.product.ServerResource;
import org.hyperic.hq.product.ServiceResource;
import org.hyperic.util.config.ConfigResponse;

public class DynomiteDetector extends DaemonDetector {

    private static final String WEB_PORT = "web_port";
    private static final String OPT_CONFIG = "config";

    private Map<String,String> opts;

    private void setOpt(Map<String,String> opts, String opt, String val) {
        if (opts.containsKey(opt) || //set by process                                                         
            (getTypeProperty(opt) != null)) //set in agent.properties                                         
        {
            return; //dont override user config                                                               
        }
        opts.put(opt, val);
    }

    //set log/config track options
    //XXX super class should take care of this via getProcOpts()
    protected ServerResource newServerResource(long pid, String exe) {
        ServerResource server = super.newServerResource(pid, exe);
        ConfigResponse config = server.getMeasurementConfig();
        String[] options = {
            ConfigFileTrackPlugin.PROP_FILES_SERVER,
        };

        for (String name : options) {
            String opt = getTypeProperty(name + ".opt");
            if (opt == null) {
                continue;
            }
            String val = this.opts.get(opt);
            if (val != null) {
                config.setValue(name, val);
            }
        }

        server.setMeasurementConfig(config);
        return server;
    }

    private String xtrim(String str, char c) {
        str = str.trim();
        int ix = str.lastIndexOf(c);
        if (ix == -1) {
            return str;
        }
        return str.substring(1, ix).trim();
    }

    private boolean parseConfig(String file) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            HashMap config =
                mapper.readValue(new File(file), HashMap.class);
            getLog().debug("Parsed " + file);
            for (Object key : config.keySet()) {
                this.opts.put(key.toString(), config.get(key).toString());
            }
        } catch (Exception e) {
            getLog().error("Cannot parse " + file + ": " + e);
        }

        return false;
    }

    private void parseOpts(long pid) {
        String cwd = getProcCwd(pid);
        String[] args = getProcArgs(pid);
        String file = null;
        for (int i=0; i<args.length; i++) {
            String arg = args[i];
            if (arg.equals(OPT_CONFIG)) {
                file = args[i+1];
            }
        }
        if (file == null) {
            getLog().warn("Unable to find config file for pid=" + pid);
            return;
        }
        if (cwd == null) {
            File root = new File(args[0]).getParentFile(); //beam
            while (root != null) {
                if (new File(root, file).exists()) {
                    cwd = root.getPath();
                    break;
                }
                root = root.getParentFile();
            }
        }
        if (cwd == null) {
            getLog().warn("Unable to find determine cwd for pid=" + pid);
            return;
        }

        setOpt(opts, INSTALLPATH, cwd);
        setOpt(opts, "instance", "dynomite@" + getPlatformName());
        StringBuilder configs = new StringBuilder();

        String arg = xtrim(file, '"');
        String ini = cwd + File.separator + arg;
        if (parseConfig(ini)) {
            configs.append(arg);
        }
    }

    private void parseOpts(String file) {
        parseConfig(file);
    }

    protected Map<String,String> getProcOpts(long pid) {
        this.opts = super.getProcOpts(pid);
        String defaultPort = getTypeProperty(Collector.PROP_PORT);

        setOpt(opts, INVENTORY_ID, "dynomite@%port%");

        String ini = opts.get(OPT_CONFIG);
        if (ini != null) {
            parseOpts(ini);
        }
        else {
            parseOpts(pid);
        }

        if (opts.get(WEB_PORT) == null) {
            opts.put(WEB_PORT, defaultPort);
        }
        //append http port for unique name
        if (!opts.get(WEB_PORT).equals(defaultPort)) {
            setOpt(opts, AUTOINVENTORY_NAME,
                   "%platform.name% " + getTypeInfo().getName() + " @%port%");
        }

        return opts;
    }

    protected List<ServiceResource> discoverServices(ConfigResponse config)
        throws PluginException {

        List<ServiceResource> services = new ArrayList<ServiceResource>();
        String hostname = config.getValue(Collector.PROP_HOSTNAME);
        int port = Integer.valueOf(config.getValue(Collector.PROP_PORT));
        //XXX
        return services;
    }
}
