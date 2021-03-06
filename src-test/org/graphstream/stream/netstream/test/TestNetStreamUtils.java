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
package org.graphstream.stream.netstream.test;

import org.graphstream.stream.netstream.NetStreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * @since 23/01/16.
 */
public class TestNetStreamUtils {
    String randomChars = "abcdefghijklmnopqrstuvwxzyABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-*+_";

    protected String getRandomString(int size) {
        String s = "";

        for (int i = 0; i < size; i++) {
            int ind = (int) ((randomChars.length() - 1) * Math.random());
            s += randomChars.substring(ind, ind + 1);
        }

        return s;
    }

    @Test
    public void testVarintSize() {
        int p = 7;

        for (int i = 1; i < 9; i++) {
            long l = (1L << p) - 1;

            Assert.assertEquals(i, NetStreamUtils.getVarintSize(l));
            Assert.assertEquals(i + 1, NetStreamUtils.getVarintSize(l + 1));

            p += 7;
        }
    }

    @Test
    public void testEncodeDecodeString() {
        for (int i = 0; i < 100; i++) {
            String s = getRandomString(64);
            ByteBuffer bb = NetStreamUtils.encodeString(s);
            String r = NetStreamUtils.decodeString(bb);

            Assert.assertEquals(s, r);
        }
    }
}
