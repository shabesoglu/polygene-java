/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.api.io;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Utility methods for creating standard Outputs
 */
public class Outputs
{
    /**
     * Write lines to a text file. Separate each line with a newline ("\n" character). If the writing or sending fails,
     * the file is deleted.
     *
     * @param file the file to save the text to
     * @return an Output for storing text in a file
     */
    public static Output<String, IOException> text( final File file )
    {
        return new Output<String, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<String, SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                final BufferedWriter writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( file ), "UTF-8" ) );

                try
                {
                    sender.sendTo( new Receiver<String, IOException>()
                    {
                        public void receive( String item ) throws IOException
                        {
                            writer.append( item ).append( '\n' );
                        }
                    } );
                    writer.close();
                } catch (IOException e)
                {
                    // We failed writing - close and delete
                    writer.close();
                    file.delete();
                } catch (Throwable senderThrowableType)
                {
                    // We failed writing - close and delete
                    writer.close();
                    file.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    /**
     * Write ByteBuffer data to a file. If the writing or sending of data fails the file will be deleted.
     *
     * @param file
     * @param <T>
     * @return
     */
    public static <T> Output<ByteBuffer, IOException> byteBuffer( final File file )
    {
        return new Output<ByteBuffer, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<ByteBuffer, SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                FileOutputStream stream = new FileOutputStream( file );
                final FileChannel fco = stream.getChannel();

                try
                {
                    sender.sendTo( new Receiver<ByteBuffer, IOException>()
                    {
                        public void receive( ByteBuffer item ) throws IOException
                        {
                            fco.write( item );
                        }
                    } );
                    stream.close();
                } catch (IOException e)
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();
                } catch (Throwable senderThrowableType)
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    /**
     * Write byte array data to a file. If the writing or sending of data fails the file will be deleted.
     *
     * @param file
     * @param bufferSize
     * @param <T>
     * @return
     */
    public static <T> Output<byte[], IOException> bytes( final File file, final int bufferSize )
    {
        return new Output<byte[], IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<byte[], SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                final OutputStream stream = new BufferedOutputStream( new FileOutputStream( file ), bufferSize );

                try
                {
                    sender.sendTo( new Receiver<byte[], IOException>()
                    {
                        public void receive( byte[] item ) throws IOException
                        {
                            stream.write( item );
                        }
                    } );
                    stream.close();
                } catch (IOException e)
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();
                } catch (Throwable senderThrowableType)
                {
                    // We failed writing - close and delete
                    stream.close();
                    file.delete();

                    throw (SenderThrowableType) senderThrowableType;
                }
            }
        };
    }

    /**
     * Write strings to System.out.println.
     *
     * @return
     */
    public static Output<String, IOException> systemOut()
    {
        return new Output<String, IOException>()
        {
            public <SenderThrowableType extends Throwable> void receiveFrom( final Sender<String, SenderThrowableType> sender ) throws IOException, SenderThrowableType
            {
                sender.sendTo( new Receiver<String, IOException>()
                {
                    public void receive( String item ) throws IOException
                    {
                        System.out.println( item );
                    }
                } );
            }
        };
    }

}
