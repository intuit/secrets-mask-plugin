package com.intuit.dev.build.ibp;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;

public class SecretsMaskOutputStreamTest {
    @Rule
    public JenkinsRule r = new JenkinsRule();

    private DummyOutputStream delegate;
    private OutputStream spyDelegate;
    private OutputStream secretsMaskOutputStream;
    private String dataString = "This is an example line with a secret password=1234\n";
    private String dataStringMasked = "This is an example line with a secret password=*****34\n";
    private byte[] data;
    private String runName = "testRun";

    @Before
    public void setup() {
        delegate = new DummyOutputStream();
        spyDelegate = spy(delegate);
        secretsMaskOutputStream = new SecretsMaskOutputStream(delegate, runName);
        data = dataString.getBytes();
    }

    @After
    public void tearDown() throws Exception {
        secretsMaskOutputStream.close();
    }

    @Test
    public void testPassThroughByteArrayWithNoSecrets() throws Exception {
        secretsMaskOutputStream.write(data);
        assertEquals(dataString, new String(delegate.getByteArray()));
        assertArrayEquals(data, delegate.getByteArray());
    }

    @Test
    public void testPassThoughByteArrayWithSecrets() throws Exception {
        // add secret
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        SecretPattern sp = new SecretPattern("Secret Password", "1234");
        config.addSecretPattern(sp);
        // mock up masked data to compare to
        byte[] dataMasked = dataStringMasked.getBytes();

        secretsMaskOutputStream.write(data);
        assertEquals(dataStringMasked, new String(delegate.getByteArray()));
        assertArrayEquals(dataMasked, delegate.getByteArray());
    }

    @Test
    public void testPassThroughByteArrayMaskingOff() throws Exception {
        // add secret
        GlobalSecretPatternsConfig config = GlobalSecretPatternsConfig.get();
        SecretPattern sp = new SecretPattern("Secret Password", "1234");
        config.addSecretPattern(sp);
        // toggle off masking
        config.setMaskDisabled(true);

        secretsMaskOutputStream.write(data);
        assertEquals(dataString, new String(delegate.getByteArray()));
        assertArrayEquals(data, delegate.getByteArray());

    }

    public static byte[] trim(byte[] data) {
        int idx = 0;
        while (idx < data.length) {
            if (data[idx] != 0) {
                break;
            }
            idx++;
        }
        byte[] trimmedData;
        if (idx > 0 && idx < data.length) {
            trimmedData = Arrays.copyOfRange(data, idx, data.length);
        } else {
            trimmedData = data;
        }
        return trimmedData;
    }

    private class DummyOutputStream extends FilterOutputStream {
        public DummyOutputStream() {
            super(new ByteArrayOutputStream());
        }

        public byte[] getByteArray() {
            return ((ByteArrayOutputStream)this.out).toByteArray();
        }
    }
}
