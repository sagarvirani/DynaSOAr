package org.dynasoar.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import org.apache.log4j.Logger;
import org.dynasoar.sync.ChangeEvent;
import org.dynasoar.sync.DirectoryWatcher;
import org.dynasoar.config.Configuration;
import java.util.*;
import java.util.logging.Level;

/**
 * ServiceMonitor is responsible for monitoring changes in Service config files.
 * It is supposed to act on and notify NodeCommunicator of any change in
 * Service.
 *
 * @author Rakshit Menpara
 */
public class ServiceMonitor implements Runnable {

    private static ServiceMonitor current = null;
    private static Logger logger = Logger.getLogger(ServiceMonitor.class);
    private static Thread th = null;
    private static HashMap<String, DynasoarService> serviceMap = new HashMap<String, DynasoarService>();

    public static void start() {
        // TODO: Start this in a separate thread
        current = new ServiceMonitor();
        th = new Thread(current, "ServiceMonitor");
        th.start();
    }

    public static boolean isRunning() {
        if (current == null) {
            return false;
        }

        return th.isAlive();
    }

    @Override
    public void run() {

        // Reads "serviceConfigDir" from configuration and starts listening
        // to the directory
        String serviceConfigDirPath = Configuration.getConfig("serviceConfigDir");
        DirectoryWatcher dir = new DirectoryWatcher(
                new ServiceConfigChangeEvent());
        dir.watch(serviceConfigDirPath);

        // In case of any changes in directory, Read service config file,
        // load/re-deploy the service on local server

        // Notify NodeCommunicator of all the changes occurred

    }

    /**
     * Implements ChangeEvent interface, which will handle directory change
     * events of Service Config Directory
     *
     * @author Rakshit Menpara
     */
    public static class ServiceConfigChangeEvent implements ChangeEvent {

        @Override
        public void fileCreated(String path) {
            DynasoarService service = null;
            try {
                service = this.readServiceConfig(path);
                if (service.getdeployed().equals("true")) {

                    // Copies the relative WAR file from WARdir directory to the deploy directory

                    String delims = "[.]";
                    String[] tokens = path.split(delims);
                    String warpath = tokens[0] + ".war";
                    File warfile = new File("sample/WARdir/" + warpath);
                    File deployfile = new File("sample/deploy/" + warpath);

                    this.copyFile(warfile, deployfile);
                }
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ServiceMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            serviceMap.put(service.getShortName(), service);
        }

        @Override
        public void fileModified(String path) {
            DynasoarService service = null;
            try {
                service = this.readServiceConfig(path);
                if (service.getdeployed().equals("true")) {

                    // Copies the relative WAR file from WARdir directory to the deploy directory

                    String delims = "[.]";
                    String[] tokens = path.split(delims);
                    String warpath = tokens[0] + ".war";
                    File warfile = new File("sample/WARdir/" + warpath);
                    File deployfile = new File("sample/deploy/" + warpath);

                    this.copyFile(warfile, deployfile);
                }

            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ServiceMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            serviceMap.put(service.getShortName(), service);
        }

        @Override
        public void fileRemoved(String path) {
            // TODO: Correct
            DynasoarService service = null;
            try {
                service = this.readServiceConfig(path);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(ServiceMonitor.class.getName()).log(Level.SEVERE, null, ex);
            }
            serviceMap.remove(service.getShortName());
        }

        private DynasoarService readServiceConfig(String path) throws IOException {
            DynasoarService service = new DynasoarService();

            //Reads and parse the config file using JSON parser (jackson)

            String relpath = "sample/serviceConfig/" + path;
            Configuration.readConfiguration(relpath);
            service.setShortName(Configuration.getConfig("name"));
            service.setdeployed(Configuration.getConfig("deployed"));

            return service;
        }

        public void copyFile(File sourceFile, File destFile) throws IOException {
            if (!destFile.exists()) {
                destFile.createNewFile();
            }

            FileChannel source = null;
            FileChannel destination = null;

            try {
                source = new FileInputStream(sourceFile).getChannel();
                destination = new FileOutputStream(destFile).getChannel();
                destination.transferFrom(source, 0, source.size());
            } finally {
                if (source != null) {
                    source.close();
                }
                if (destination != null) {
                    destination.close();
                }
            }
        }
    }
}
