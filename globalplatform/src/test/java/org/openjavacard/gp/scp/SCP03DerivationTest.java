/*
 * openjavacard-tools: Development tools for JavaCard
 * Copyright (C) 2018 Ingo Albrecht <copyright@promovicz.org>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

package org.openjavacard.gp.scp;

import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.openjavacard.gp.keys.GPKey;
import org.openjavacard.gp.keys.GPKeyCipher;
import org.openjavacard.gp.keys.GPKeySet;
import org.openjavacard.gp.keys.GPKeyUsage;
import org.openjavacard.util.HexUtil;

@RunWith(BlockJUnit4ClassRunner.class)
public class SCP03DerivationTest extends TestCase {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(SCP03DerivationTest.class);
    }

    @Test
    public void testDeriveSCP03_000010() {
        byte[] cardSequence = HexUtil.hexToBytes("000010");
        byte[] hostChallenge = HexUtil.hexToBytes("A7F76C713F0A713D");
        byte[] cardChallenge = HexUtil.hexToBytes("31900058C1C451A2");

        // perform derivation
        GPKeySet derived = SCP03Derivation.deriveSessionKeys(GPKeySet.GLOBALPLATFORM, cardSequence, hostChallenge, cardChallenge);

        // check the key version
        Assert.assertEquals(0, derived.getKeyVersion());

        // check the ENC key
        GPKey encKey = derived.getKeyByUsage(GPKeyUsage.ENC);
        Assert.assertEquals(GPKeyUsage.ENC, encKey.getUsage());
        Assert.assertEquals(GPKeyCipher.AES, encKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("258a78866f41482bef482dc8ca976ccd"), encKey.getSecret());

        // check the MAC key
        GPKey macKey = derived.getKeyByUsage(GPKeyUsage.MAC);
        Assert.assertEquals(GPKeyUsage.MAC, macKey.getUsage());
        Assert.assertEquals(GPKeyCipher.AES, macKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("053db6abc7fdf3b63a0d965ee16b0255"), macKey.getSecret());

        // check the RMAC key
        GPKey rmacKey = derived.getKeyByUsage(GPKeyUsage.RMAC);
        Assert.assertEquals(GPKeyUsage.RMAC, rmacKey.getUsage());
        Assert.assertEquals(GPKeyCipher.AES, rmacKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("eda0b4f2ec0345bfc50f3bc59cfef936"), rmacKey.getSecret());

        // the KEK is a copy of the master key
        GPKey kekKey = derived.getKeyByUsage(GPKeyUsage.KEK);
        Assert.assertEquals(GPKeyUsage.KEK, kekKey.getUsage());
        Assert.assertEquals(GPKeyCipher.AES, kekKey.getCipher());
        Assert.assertArrayEquals(HexUtil.hexToBytes("404142434445464748494A4B4C4D4E4F"), kekKey.getSecret());
    }

}
