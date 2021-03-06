package nodebox.node;

import nodebox.util.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import static nodebox.util.Preconditions.checkArgument;
import static nodebox.util.Preconditions.checkNotNull;

@Description("Generic node")
public abstract class Node {

    public static final HashMap<Class, Integer> instanceCounts = new HashMap<Class, Integer>();

    private Network network;
    private String name = "";
    private String displayName;
    private Point position = new Point(0, 0);
    private LinkedList<Port> ports = new LinkedList<Port>();

    public static int createInstance(Class c) {
        synchronized (instanceCounts) {
            Integer index = instanceCounts.get(c);
            if (index == null) {
                index = 1;
            } else {
                index += 1;
            }
            instanceCounts.put(c, index);
            return index;
        }
    }

    public Node() {
        name = Strings.classToName(getClass());
        setDisplayName(defaultDisplayName());
    }

    public boolean canDraw() {
        return false;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        checkNotNull(displayName);
        checkArgument(displayName.trim().length() != 0, "Display name cannot be empty.");
        this.displayName = displayName;
    }

    public String defaultDisplayName() {
        String className = getClass().getSimpleName();
        return Strings.humanizeName(className);
    }

    public boolean hasDefaultDisplayName() {
        return defaultDisplayName().equals(displayName);
    }

    public String getDescription() {
        Description description = getClass().getAnnotation(Description.class);
        if (description != null) {
            return description.value();
        } else {
            return "";
        }
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public void setPosition(int x, int y) {
        this.position = new Point(x, y);
    }

    public boolean isRenderedNode() {
        return network != null && network.getRenderedNode() == this;
    }

    public void setRenderedNode() {
        if (network != null) {
            network.setRenderedNode(this);
        }
    }

    //// Ports ////

    public java.util.List<Port> getPorts() {
        return ports;
    }

    public java.util.List<Port> getInputPorts() {
        ArrayList<Port> inputPorts = new ArrayList<Port>();
        for (Port port : ports) {
            if (port.getDirection() == Port.Direction.INPUT) {
                inputPorts.add(port);
            }
        }
        return inputPorts;
    }

    public java.util.List<Port> getOutputPorts() {
        ArrayList<Port> inputPorts = new ArrayList<Port>();
        for (Port port : ports) {
            if (port.getDirection() == Port.Direction.OUTPUT) {
                inputPorts.add(port);
            }
        }
        return inputPorts;
    }

    /**
     * Add a port to this node.
     * <p/>
     * The port.node should be set to this node, and the port name should be unique.
     *
     * @param port the port to add.
     * @return the given port
     * @throws IllegalArgumentException if the port name is not unique or the node is not set to this node.
     */
    public Port addPort(Port port) {
        checkArgument(port.getNode() == this, "Port.node should be set to this.");
        checkArgument(!hasPort(port.getName()), "This node already has a port named %s", port.getName());
        ports.add(port);
        return port;
    }

    /**
     * Remove the given port from this node.
     *
     * @param port the port to remove.
     */
    public void removePort(Port port) {
        network.disconnect(port);
        ports.remove(port);
    }

    /**
     * Remove the port with the given name.
     *
     * @param name the name of the port.
     * @throws IllegalArgumentException if a port with the given name could not be found.
     */
    public void removePort(String name) throws IllegalArgumentException {
        Port port = getPort(name);
        removePort(port);
    }

    /**
     * Check if this node has a port with the given name.
     *
     * @param name the name to check.
     * @return true if this node has a port with the given name.
     */
    public boolean hasPort(String name) {
        for (Port p : ports) {
            if (p.getName().equals(name)) return true;
        }
        return false;
    }

    /**
     * Get the port with the given name.
     *
     * @param name the port name.
     * @return the port object
     * @throws IllegalArgumentException if a port with the given name does not exist.
     */
    public Port getPort(String name) throws IllegalArgumentException {
        checkNotNull(name);
        for (Port p : ports) {
            if (p.getName().equals(name)) return p;
        }
        throw new IllegalArgumentException("This node has no port named " + name);
    }

    //// Port values ////

    public void setValue(String portName, Object value) {
        Port port = getPort(portName);
        port.setValue(value);
    }

    public Object getValue(String portName) {
        Port port = getPort(portName);
        return port.getValue();
    }

    public Object parseValue(String portName, String value) {
        checkNotNull(value);
        Port port = getPort(portName);
        if (port.isPersistable()) {
            PersistablePort persistablePort = (PersistablePort) port;
            return persistablePort.parseValue(value);
        } else {
            return null;
        }
    }

    //// Custom interface ////

    public JComponent createCustomEditor() {
        return null;
    }

    /**
     * Get a list of all custom keys for this node.
     *
     * @return a list of custom keys.
     */
    public java.util.List<String> getCustomKeys() {
        return Collections.emptyList();
    }

    /**
     * Write out extra data for this node.
     * <p/>
     * This hook exists so that nodes can save additional data not saved in ports, such as custom UI or settings.
     * <p/>
     * The default implementation returns null, indicating that no extra data needs to be serialized.
     * If you implement serialize, also implement deserialize to load the data back into your node.
     *
     * @param key the key to serialize
     * @return a string with data that needs to be written to the file.
     * @see #deserializeCustomValue(String, String)
     */
    public String serializeCustomValue(String key) {
        return null;
    }

    /**
     * Load extra data for this node that was previously saved in a file.
     * <p/>
     * This hook exists so that nodes can save additional data not saved in ports, such as custom UI or settings.
     * <p/>
     * The default implementation does not act on the data, because no extra is serialized.
     * If you implement deserialize, also implement serialize to save the custom data from your node.
     *
     * @param key   the key to deserialize
     * @param value the value to deserialize
     * @see #serializeCustomValue(String)
     */
    public void deserializeCustomValue(String key, String value) {

    }

    //// Execution ////

    public boolean startExecution(Context context) {
        return true;
    }

    public void stopExecution(Context context) {
    }

    /**
     * Call this method to update a node and its dependencies.
     * <p/>
     * This method will:
     * <ul>
     * <li>Update its dependencies</li>
     * <li>Execute itself</li>
     * </ul>
     *
     * @param context the processing context
     * @param time    the current time
     */
    public void update(Context context, float time) {
        updateDependencies(context, time);
        execute(context, time);
    }

    public void updateDependencies(Context context, float time) {
        if (network == null) return;
        for (Connection c : network.getInputConnections(this)) {
            Node n = c.getOutputNode();
            if (!context.hasExecuted(n)) {
                context.addToExecutedNodes(n);
                n.update(context, time);
            }
            c.getInputPort().setValue(c.getOutputPort().getValue());
        }
    }


    /**
     * Process the node and update the values of the output ports.
     *
     * @param context the drawing context
     * @param time    the current time
     */
    public abstract void execute(Context context, float time);

    @Override
    public String toString() {
        return "Node[" + name + "]";
    }
}
