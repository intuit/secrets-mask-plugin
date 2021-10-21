package com.intuit.dev.build.ibp;


import hudson.util.ByteArrayOutputStream2;
import org.apache.commons.lang.StringUtils;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.logging.Logger;



// Writes to a buffer branch to get contextual data for masking secrets, then writes to underlying OutputStream once new line or buffer full
public class SecretsMaskOutputStream extends FilterOutputStream {
    private static final int RECEIVE_BUFFER_SIZE = 512;
    private static final Logger LOGGER = Logger.getLogger(SecretsMaskOutputStream.class.getName());
    private ByteArrayOutputStream2 branch = new ByteArrayOutputStream2(RECEIVE_BUFFER_SIZE);
    private String runName;
    // maximum line length (very long lines are, however, often a sign of garbage data)
    // if it is increased, please also increase the TRUNCATE config in splunk props.conf
    // ref: http://docs.splunk.com/Documentation/Splunk/7.2.1/Admin/Propsconf
    public static final int CONSOLE_TEXT_SINGLE_LINE_MAX_LENGTH = Integer.getInteger("splunkins.lineTruncate", 100000);

    public SecretsMaskOutputStream(OutputStream outputStream, String runName) {
        super(outputStream);
        this.runName = runName;
    }

    @Override
    public void write(int b) throws IOException {
        branch.write(b);
        if (b == '\n' || branch.size() > CONSOLE_TEXT_SINGLE_LINE_MAX_LENGTH) {
            eol();
        }
    }

    protected void eol() throws IOException {
        if (!GlobalSecretPatternsConfig.get().isMaskDisabled()) {
            String line = branch.toString(Charset.defaultCharset().toString());
            // reuse the buffer under normal circumstances
            branch.reset();
            if (StringUtils.isNotBlank(line)) {
                line = SecretsMaskUtil.secretsMaskGlobal(line, runName);
            }
            byte[] b = line.getBytes("UTF-8");
            superWrite(b, 0, b.length);
        } else {
            superWrite(branch.getBuffer(), 0, branch.size());
            branch.reset();
        }
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }


    // Helper method to call FilterOutputStream.write(int) to underlying output stream
    // need to call FilterOutputStream.write(int) directly else
    // calling FilterOutputStream.write(byte[]) will call SecretsMaskOutputStream.write(int) (self referential loop)
    private void superWrite(byte[] b, int off, int len) throws IOException {
        if ((off | len | (b.length - (len + off)) | (off + len)) < 0)
            throw new IndexOutOfBoundsException();

        for (int i = 0 ; i < len ; i++) {
            super.write(b[off + i]);
        }
    }
}
