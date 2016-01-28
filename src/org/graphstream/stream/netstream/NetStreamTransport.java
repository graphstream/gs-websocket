package org.graphstream.stream.netstream;

import java.nio.ByteBuffer;

/**
 * @author n3mo
 * @since 22/01/16.
 */
public interface NetStreamTransport {
    void send(ByteBuffer buffer);
}
