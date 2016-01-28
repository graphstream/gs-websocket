/*
 * Copyright 2006 - 2016
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 *
 * This file is part of GraphStream <http://graphstream-project.org>.
 *
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 *
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.stream.webSocket;

import org.graphstream.stream.Pipe;
import org.graphstream.stream.Replayable;
import org.graphstream.stream.SourceBase;
import org.graphstream.stream.netstream.NetStreamEncoder;
import org.graphstream.stream.netstream.NetStreamTransport;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

/**
 * @since 22/01/16.
 */
public class WebSocketPipe extends SourceBase implements Pipe {
    private static final Logger LOGGER = Logger.getLogger(WebSocketPipe.class.getName());

    public static final int DEFAULT_PORT = 10042;

    protected WSServer server;
    protected NetStreamEncoder encoder;

    protected LinkedList<WebSocketFilter> filters;

    protected Replayable replayable;

    public WebSocketPipe() throws UnknownHostException {
        this(DEFAULT_PORT);
    }

    public WebSocketPipe(int port) throws UnknownHostException {
        this(null, port);
    }

    public WebSocketPipe(Replayable replayable) throws UnknownHostException {
        this(replayable, DEFAULT_PORT);
    }

    public WebSocketPipe(Replayable replayable, int port) throws UnknownHostException {
        server = new WSServer(port);
        encoder = new NetStreamEncoder("wss", server);
        filters = new LinkedList<WebSocketFilter>();

        this.replayable = replayable;
    }

    public void addWebSocketFilter(WebSocketFilter filter) {
        synchronized (filters) {
            filters.add(filter);
        }
    }

    public void removeWebSocketFilter(WebSocketFilter filter) {
        synchronized (filters) {
            filters.remove(filter);
        }
    }

    public void startServer() {
        server.start();
        SERVERS.offer(server);
        LOGGER.info(String.format("WebSocket Server is listenning on %d", server.getPort()));
    }

    public void stopServer() throws InterruptedException {
        try {
            server.stop();
            SERVERS.remove(server);
        } catch (IOException e) {
            LOGGER.warning("exception while stopping ws server: " + e.getMessage());
        }
    }

    @Override
    public void graphAttributeAdded(String sourceId, long timeId, String attribute, Object value) {
        encoder.graphAttributeAdded(sourceId, timeId, attribute, value);
    }

    @Override
    public void graphAttributeChanged(String sourceId, long timeId, String attribute, Object oldValue, Object newValue) {
        encoder.graphAttributeChanged(sourceId, timeId, attribute, oldValue, newValue);
    }

    @Override
    public void graphAttributeRemoved(String sourceId, long timeId, String attribute) {
        encoder.graphAttributeRemoved(sourceId, timeId, attribute);
    }

    @Override
    public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
        encoder.nodeAttributeAdded(sourceId, timeId, nodeId, attribute, value);
    }

    @Override
    public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
        encoder.nodeAttributeChanged(sourceId, timeId, nodeId, attribute, oldValue, newValue);
    }

    @Override
    public void nodeAttributeRemoved(String sourceId, long timeId, String nodeId, String attribute) {
        encoder.nodeAttributeRemoved(sourceId, timeId, nodeId, attribute);
    }

    @Override
    public void edgeAttributeAdded(String sourceId, long timeId, String edgeId, String attribute, Object value) {
        encoder.edgeAttributeAdded(sourceId, timeId, edgeId, attribute, value);
    }

    @Override
    public void edgeAttributeChanged(String sourceId, long timeId, String edgeId, String attribute, Object oldValue, Object newValue) {
        encoder.edgeAttributeChanged(sourceId, timeId, edgeId, attribute, oldValue, newValue);
    }

    @Override
    public void edgeAttributeRemoved(String sourceId, long timeId, String edgeId, String attribute) {
        encoder.edgeAttributeRemoved(sourceId, timeId, edgeId, attribute);
    }

    @Override
    public void nodeAdded(String sourceId, long timeId, String nodeId) {
        encoder.nodeAdded(sourceId, timeId, nodeId);
    }

    @Override
    public void nodeRemoved(String sourceId, long timeId, String nodeId) {
        encoder.nodeRemoved(sourceId, timeId, nodeId);
    }

    @Override
    public void edgeAdded(String sourceId, long timeId, String edgeId, String fromNodeId, String toNodeId, boolean directed) {
        encoder.edgeAdded(sourceId, timeId, edgeId, fromNodeId, toNodeId, directed);
    }

    @Override
    public void edgeRemoved(String sourceId, long timeId, String edgeId) {
        encoder.edgeRemoved(sourceId, timeId, edgeId);
    }

    @Override
    public void graphCleared(String sourceId, long timeId) {
        encoder.graphCleared(sourceId, timeId);
    }

    @Override
    public void stepBegins(String sourceId, long timeId, double step) {
        encoder.stepBegins(sourceId, timeId, step);
    }

    private static final ConcurrentLinkedQueue<WSServer> SERVERS = new ConcurrentLinkedQueue<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (SERVERS.size() > 0) {
                    System.out.print("Stopping remaining WebSocket servers...");

                    while (SERVERS.size() > 0) {
                        try {
                            SERVERS.poll().stop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    System.out.println("  done");
                }
            }
        }));
    }

    class WSServer extends WebSocketServer implements NetStreamTransport {
        public WSServer(int port) throws UnknownHostException {
            this(new InetSocketAddress(port));
        }

        public WSServer(InetSocketAddress address) {
            super(address);
        }

        @Override
        public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
            LOGGER.info("websocket opened: " + webSocket.getRemoteSocketAddress());

            synchronized (filters) {
                for (WebSocketFilter filter : filters) {
                    if (!filter.authorizeWebSocketConnection(webSocket)) {
                        webSocket.close();
                        return;
                    }
                }
            }

            replay(webSocket);
        }

        @Override
        public void onClose(WebSocket webSocket, int i, String s, boolean b) {
            LOGGER.info("webSocket closed: " + webSocket.getRemoteSocketAddress());
        }

        @Override
        public void onMessage(WebSocket webSocket, String s) {
            LOGGER.info("receive message: " + s);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteBuffer buffer) {
            LOGGER.info("receive binary data");
        }

        @Override
        public void onError(WebSocket webSocket, Exception e) {
            LOGGER.warning("webSocket error : " + (webSocket == null ? "" : webSocket.getRemoteSocketAddress()) + ", " + e.getClass().getName() + " : " + e.getMessage());
            e.printStackTrace();
        }

        void sendToAll(String text) {
            Collection<WebSocket> con = connections();

            synchronized (con) {
                for (WebSocket c : con) {
                    c.send(text);
                }
            }
        }

        @Override
        public void send(ByteBuffer buffer) {
            Collection<WebSocket> con = connections();

            synchronized (con) {
                for (WebSocket ws : con) {
                    buffer.rewind();
                    ws.send(buffer);
                }
            }
        }

        public void replay(final WebSocket webSocket) {
            if (replayable != null) {
                Replayable.Controller c = replayable.getReplayController();
                NetStreamEncoder netStreamEncoder = new NetStreamEncoder("wss-replay", new NetStreamTransport() {
                    @Override
                    public void send(ByteBuffer buffer) {
                        webSocket.send(buffer);
                    }
                });

                c.addSink(netStreamEncoder);
                c.replay();
            }
        }
    }
}
