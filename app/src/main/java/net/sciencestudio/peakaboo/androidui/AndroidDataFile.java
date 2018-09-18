package net.sciencestudio.peakaboo.androidui;

import android.content.res.AssetFileDescriptor;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import peakaboo.datasource.model.DataSource;
import peakaboo.datasource.model.datafile.DataFile;

public class AndroidDataFile implements DataFile {

    private AssetFileDescriptor fd;
    private String filename;
    private Path tempFolder;
    private Path tempPath = null;

    public AndroidDataFile(AssetFileDescriptor fd, String filename, Path tempFolder) {
        this.fd = fd;
        this.filename = filename;
        this.tempFolder = tempFolder;
    }

    /**
     * Gets a relative filename for this DataFile.
     */
    @Override
    public String getFilename() {
        return filename;
    }

    /**
     * Returns an {@link InputStream} for this DataFile
     *
     * @throws IOException
     */
    @Override
    public InputStream getInputStream() throws IOException {
        return fd.createInputStream();
    }

    /**
     * Ensures that the data is available as a file on disk, and returns a
     * {@link Path} to that file. <br/>
     * <br/>
     * Note that for some input sources, the data may not originate from a file on
     * disk, and this step may incur extra overhead compared to
     * {@link #getInputStream()}. This is useful for {@link DataSource}s which wrap
     * native libraries, and which cannot make use of Java constructs like
     * {@link InputStream}s. To minimize overhead and resource consumotion,
     * DataSources are encouraged to call {@link #close()} on a DataFile after it
     * has been read to clean up any temporary files created in the process.
     *
     * @throws IOException
     */
    @Override
    public Path getAndEnsurePath() throws IOException {

        //build new file for this content
        if (tempPath == null) {
            File f = new File(tempFolder.toString() + "/" + getFilename());
            tempPath = f.toPath();
            Files.copy(getInputStream(), tempPath);
        }
        return tempPath;

    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        if (tempPath != null) {
            tempPath.toFile().delete();
        }
    }
}
