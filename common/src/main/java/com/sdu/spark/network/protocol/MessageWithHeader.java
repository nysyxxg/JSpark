package com.sdu.spark.network.protocol;

import com.google.common.base.Preconditions;
import com.sdu.spark.network.buffer.ManagedBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.FileRegion;
import io.netty.util.AbstractReferenceCounted;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Todo:
 *  研究Netty的内存管理(http://blog.csdn.net/heishiyuriyao/article/details/49049575)
 *
 * @author hanhan.zhang
 * */
public class MessageWithHeader extends AbstractReferenceCounted implements FileRegion {

    private final ManagedBuffer managedBuffer;
    private final ByteBuf header;
    private final int headerLength;
    private final Object body;
    private final long bodyLength;
    private long totalBytesTransferred;

    /**
     * When the write buffer size is larger than this limit, I/O will be done in chunks of this size.
     * The size should not be too large as it will waste underlying memory copy. e.g. If network
     * avaliable buffer is smaller than this limit, the data cannot be sent within one single write
     * operation while it still will make memory copy with this size.
     */
    private static final int NIO_BUFFER_LIMIT = 256 * 1024;

    /**
     * Construct a new MessageWithHeader.
     *
     * @param managedBuffer the {@link ManagedBuffer} that the message body came from. This needs to
     *                      be passed in so that the buffer can be freed when this message is
     *                      deallocated. Ownership of the caller's reference to this buffer is
     *                      transferred to this class, so if the caller wants to continue to use the
     *                      ManagedBuffer in other messages then they will need to call retain() on
     *                      it before passing it to this constructor. This may be null if and only if
     *                      `body` is a {@link FileRegion}.
     * @param header the message header.
     * @param body the message body. Must be either a {@link ByteBuf} or a {@link FileRegion}.
     * @param bodyLength the length of the message body, in bytes.
     */
    MessageWithHeader(
            ManagedBuffer managedBuffer,
            ByteBuf header,
            Object body,
            long bodyLength) {
        Preconditions.checkArgument(body instanceof ByteBuf || body instanceof FileRegion,
                "Body must be a ByteBuf or a FileRegion.");
        this.managedBuffer = managedBuffer;
        this.header = header;
        this.headerLength = header.readableBytes();
        this.body = body;
        this.bodyLength = bodyLength;
    }

    @Override
    public long count() {
        return headerLength + bodyLength;
    }

    @Override
    public long position() {
        return 0;
    }

    @Override
    public long transfered() {
        return totalBytesTransferred;
    }

    /**
     * This code is more complicated than you would think because we might require multiple
     * transferTo invocations in order to transfer a single MessageWithHeader to avoid busy waiting.
     *
     * The contract is that the caller will ensure position is properly set to the total number
     * of bytes transferred so far (i.e. value returned by transfered()).
     */
    @Override
    public long transferTo(final WritableByteChannel target, final long position) throws IOException {
        Preconditions.checkArgument(position == totalBytesTransferred, "Invalid position.");
        // Bytes written for header in this call.
        long writtenHeader = 0;
        if (header.readableBytes() > 0) {
            writtenHeader = copyByteBuf(header, target);
            totalBytesTransferred += writtenHeader;
            if (header.readableBytes() > 0) {
                return writtenHeader;
            }
        }

        // Bytes written for body in this call.
        long writtenBody = 0;
        if (body instanceof FileRegion) {
            writtenBody = ((FileRegion) body).transferTo(target, totalBytesTransferred - headerLength);
        } else if (body instanceof ByteBuf) {
            writtenBody = copyByteBuf((ByteBuf) body, target);
        }
        totalBytesTransferred += writtenBody;

        return writtenHeader + writtenBody;
    }

    @Override
    protected void deallocate() {
        header.release();
        ReferenceCountUtil.release(body);
        if (managedBuffer != null) {
            managedBuffer.release();
        }
    }

    private int copyByteBuf(ByteBuf buf, WritableByteChannel target) throws IOException {
        ByteBuffer buffer = buf.nioBuffer();
        int written = (buffer.remaining() <= NIO_BUFFER_LIMIT) ?
                target.write(buffer) : writeNioBuffer(target, buffer);
        buf.skipBytes(written);
        return written;
    }

    private int writeNioBuffer(
            WritableByteChannel writeCh,
            ByteBuffer buf) throws IOException {
        int originalLimit = buf.limit();
        int ret = 0;

        try {
            int ioSize = Math.min(buf.remaining(), NIO_BUFFER_LIMIT);
            buf.limit(buf.position() + ioSize);
            ret = writeCh.write(buf);
        } finally {
            buf.limit(originalLimit);
        }

        return ret;
    }
}
