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
 * The fact that you are presently decodeing this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.stream.netstream;

import org.graphstream.stream.SourceBase;

import static org.graphstream.stream.netstream.NetStreamUtils.*;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

/**
 * @since 22/01/16.
 */
public class NetStreamDecoder2 extends SourceBase {
    private static final Logger LOGGER = Logger.getLogger(NetStreamDecoder2.class.getName());

    protected boolean debug = false;

    public void decode(ByteBuffer bb) {
        try {
            int cmd = bb.get();

            if (cmd == NetStreamConstants.EVENT_ADD_NODE) {
                serve_EVENT_ADD_NODE(bb);
            } else if ((cmd & 0xFF) == (NetStreamConstants.EVENT_DEL_NODE & 0xFF)) {
                serve_DEL_NODE(bb);
            } else if (cmd == NetStreamConstants.EVENT_ADD_EDGE) {
                serve_EVENT_ADD_EDGE(bb);
            } else if (cmd == NetStreamConstants.EVENT_DEL_EDGE) {
                serve_EVENT_DEL_EDGE(bb);
            } else if (cmd == NetStreamConstants.EVENT_STEP) {
                serve_EVENT_STEP(bb);
            } else if (cmd == NetStreamConstants.EVENT_CLEARED) {
                serve_EVENT_CLEARED(bb);
            } else if (cmd == NetStreamConstants.EVENT_ADD_GRAPH_ATTR) {
                serve_EVENT_ADD_GRAPH_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_CHG_GRAPH_ATTR) {
                serve_EVENT_CHG_GRAPH_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_DEL_GRAPH_ATTR) {
                serve_EVENT_DEL_GRAPH_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_ADD_NODE_ATTR) {
                serve_EVENT_ADD_NODE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_CHG_NODE_ATTR) {
                serve_EVENT_CHG_NODE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_DEL_NODE_ATTR) {
                serve_EVENT_DEL_NODE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_ADD_EDGE_ATTR) {
                serve_EVENT_ADD_EDGE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_CHG_EDGE_ATTR) {
                serve_EVENT_CHG_EDGE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_DEL_EDGE_ATTR) {
                serve_EVENT_DEL_EDGE_ATTR(bb);
            } else if (cmd == NetStreamConstants.EVENT_END) {
                LOGGER.info("NetStreamReceiver : Client properly ended the connection.");
            } else {
                LOGGER.warning("NetStreamReceiver: Don't know this command: " + cmd);
            }
        } catch (BufferUnderflowException e) {
            LOGGER.warning("bad buffer");
        }
    }

    /**
     * @param bb
     * @see NetStreamConstants#EVENT_DEL_EDGE
     */
    protected void serve_EVENT_DEL_EDGE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received DEL_EDGE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String edgeId = decodeString(bb);
        String attrId = decodeString(bb);

        sendEdgeAttributeRemoved(sourceId, timeId, edgeId, attrId);
    }

    /**
     * @see NetStreamConstants#EVENT_CHG_EDGE_ATTR
     */
    protected void serve_EVENT_CHG_EDGE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received CHG_EDGE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String edgeId = decodeString(bb);
        String attrId = decodeString(bb);
        int oldValueType = decodeType(bb);
        Object oldValue = decodeValue(bb, oldValueType);
        int newValueType = decodeType(bb);
        Object newValue = decodeValue(bb, newValueType);

        sendEdgeAttributeChanged(sourceId, timeId, edgeId, attrId,
                oldValue, newValue);

    }

    /**
     * @see NetStreamConstants#EVENT_ADD_EDGE_ATTR
     */
    protected void serve_EVENT_ADD_EDGE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received ADD_EDGE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String edgeId = decodeString(bb);
        String attrId = decodeString(bb);
        Object value = decodeValue(bb, decodeType(bb));

        sendEdgeAttributeAdded(sourceId, timeId, edgeId, attrId,
                value);

    }

    /**
     * @see NetStreamConstants#EVENT_DEL_NODE_ATTR
     */
    protected void serve_EVENT_DEL_NODE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received DEL_NODE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String nodeId = decodeString(bb);
        String attrId = decodeString(bb);

        sendNodeAttributeRemoved(sourceId, timeId, nodeId, attrId);

    }

    /**
     * @see NetStreamConstants#EVENT_CHG_NODE_ATTR
     */
    protected void serve_EVENT_CHG_NODE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_CHG_NODE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String nodeId = decodeString(bb);
        String attrId = decodeString(bb);
        int oldValueType = decodeType(bb);
        Object oldValue = decodeValue(bb, oldValueType);
        int newValueType = decodeType(bb);
        Object newValue = decodeValue(bb, newValueType);

        sendNodeAttributeChanged(sourceId, timeId, nodeId, attrId,
                oldValue, newValue);
    }

    /**
     * @see NetStreamConstants#EVENT_ADD_NODE_ATTR
     */
    protected void serve_EVENT_ADD_NODE_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_ADD_NODE_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String nodeId = decodeString(bb);
        String attrId = decodeString(bb);
        Object value = decodeValue(bb, decodeType(bb));

        sendNodeAttributeAdded(sourceId, timeId, nodeId, attrId,
                value);
    }

    /**
     * @see NetStreamConstants#EVENT_DEL_GRAPH_ATTR
     */
    protected void serve_EVENT_DEL_GRAPH_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_DEL_GRAPH_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String attrId = decodeString(bb);

        sendGraphAttributeRemoved(sourceId, timeId, attrId);
    }

    /**
     * @see NetStreamConstants#EVENT_CHG_GRAPH_ATTR
     */
    protected void serve_EVENT_CHG_GRAPH_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_CHG_GRAPH_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String attrId = decodeString(bb);
        int oldValueType = decodeType(bb);
        Object oldValue = decodeValue(bb, oldValueType);
        int newValueType = decodeType(bb);
        Object newValue = decodeValue(bb, newValueType);

        sendGraphAttributeChanged(sourceId, timeId, attrId, oldValue,
                newValue);
    }

    /**
     * @see NetStreamConstants#EVENT_ADD_GRAPH_ATTR
     */
    protected void serve_EVENT_ADD_GRAPH_ATTR(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_ADD_GRAPH_ATTR command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String attrId = decodeString(bb);
        Object value = decodeValue(bb, decodeType(bb));
        if (debug) {
            LOGGER.info(String.format("NetStreamServer | EVENT_ADD_GRAPH_ATTR | %s=%s", attrId,
                    value.toString()));
        }

        sendGraphAttributeAdded(sourceId, timeId, attrId, value);
    }

    /**
     * @see NetStreamConstants#EVENT_CLEARED
     */
    protected void serve_EVENT_CLEARED(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_CLEARED command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);

        sendGraphCleared(sourceId, timeId);
    }

    /**
     * @see NetStreamConstants#EVENT_STEP
     */
    protected void serve_EVENT_STEP(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_STEP command.");
        }
        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        double time = decodeDouble(bb);

        sendStepBegins(sourceId, timeId, time);
    }

    /**
     * @see NetStreamConstants#EVENT_DEL_EDGE
     */
    protected void serve_EVENT_DEL_EDGE(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_DEL_EDGE command.");
        }

        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String edgeId = decodeString(bb);

        sendEdgeRemoved(sourceId, timeId, edgeId);
    }

    /**
     * @see NetStreamConstants#EVENT_ADD_EDGE
     */
    protected void serve_EVENT_ADD_EDGE(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received ADD_EDGE command.");
        }

        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String edgeId = decodeString(bb);
        String from = decodeString(bb);
        String to = decodeString(bb);
        boolean directed = decodeBoolean(bb);

        sendEdgeAdded(sourceId, timeId, edgeId, from, to, directed);
    }

    /**
     * @see NetStreamConstants#EVENT_DEL_NODE
     */
    protected void serve_DEL_NODE(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received DEL_NODE command.");
        }

        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String nodeId = decodeString(bb);

        sendNodeRemoved(sourceId, timeId, nodeId);
    }

    /**
     * @see NetStreamConstants#EVENT_ADD_NODE
     */
    protected void serve_EVENT_ADD_NODE(ByteBuffer bb) {
        if (debug) {
            LOGGER.info("NetStreamServer: Received EVENT_ADD_NODE command");
        }

        String sourceId = decodeString(bb);
        long timeId = decodeUnsignedVarint(bb);
        String nodeId = decodeString(bb);

        sendNodeAdded(sourceId, timeId, nodeId);
    }
}
