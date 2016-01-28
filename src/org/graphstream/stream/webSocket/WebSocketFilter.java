package org.graphstream.stream.webSocket;

import org.java_websocket.WebSocket;

/***
 * @since 23/01/16.
 */
public interface WebSocketFilter {
    boolean authorizeWebSocketConnection(WebSocket webSocket);
}
