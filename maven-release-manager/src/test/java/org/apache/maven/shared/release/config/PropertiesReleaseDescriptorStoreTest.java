package org.apache.maven.shared.release.config;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.shared.release.phase.AbstractReleaseTestCase;
import org.apache.maven.shared.release.scm.IdentifiedScm;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.io.IOException;

/**
 * Test the properties store.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 */
public class PropertiesReleaseDescriptorStoreTest
    extends PlexusTestCase
{
    private PropertiesReleaseDescriptorStore store;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        store = (PropertiesReleaseDescriptorStore) lookup( ReleaseDescriptorStore.ROLE, "properties" );
    }

    public void testReadFromFile()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/release.properties" );

        ReleaseDescriptor config = store.read( file );

        ReleaseDescriptor expected = createExpectedReleaseConfiguration();

        assertEquals( "check matches", expected, config );
    }

    public void testReadFromFileUsingWorkingDirectory()
        throws Exception
    {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();
        releaseDescriptor.setWorkingDirectory( AbstractReleaseTestCase.getPath(  getTestFile( "target/test-classes" ) ) );
        ReleaseDescriptor config = store.read( releaseDescriptor );

        ReleaseDescriptor expected = createExpectedReleaseConfiguration();
        expected.setWorkingDirectory( releaseDescriptor.getWorkingDirectory() );

        assertEquals( "check matches", expected, config );
    }

    public void testReadFromEmptyFile()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/empty-release.properties" );

        ReleaseDescriptor config = store.read( file );

        assertDefaultReleaseConfiguration( config );
    }

    public void testReadMissingFile()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/no-release.properties" );

        ReleaseDescriptor config = store.read( file );

        assertDefaultReleaseConfiguration( config );
    }

    public void testMergeFromEmptyFile()
        throws ReleaseDescriptorStoreException, IOException
    {
        File file = getTestFile( "target/test-classes/empty-release.properties" );

        ReleaseDescriptor mergeDescriptor = createMergeConfiguration();
        ReleaseDescriptor config = store.read( mergeDescriptor, file );

        assertEquals( "Check configurations merged", mergeDescriptor, config );
    }

    public void testMergeFromMissingFile()
        throws ReleaseDescriptorStoreException, IOException
    {
        File file = getTestFile( "target/test-classes/no-release.properties" );

        ReleaseDescriptor mergeDescriptor = createMergeConfiguration();
        ReleaseDescriptor config = store.read( mergeDescriptor, file );

        assertEquals( "Check configurations merged", mergeDescriptor, config );
    }

    public void testWriteToNewFile()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/new-release.properties" );
        file.delete();
        assertFalse( "Check file doesn't exist", file.exists() );

        ReleaseDescriptor config = createReleaseConfigurationForWriting();

        store.write( config, file );

        ReleaseDescriptor rereadDescriptor = store.read( file );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testWriteToWorkingDirectory()
        throws Exception
    {
        File file = getTestFile( "target/test-classes/new/release.properties" );
        file.delete();
        assertFalse( "Check file doesn't exist", file.exists() );
        file.getParentFile().mkdirs();

        ReleaseDescriptor config = createReleaseConfigurationForWriting();
        config.setWorkingDirectory( AbstractReleaseTestCase.getPath( file.getParentFile() ) );

        store.write( config );

        ReleaseDescriptor rereadDescriptor = store.read( file );
        rereadDescriptor.setWorkingDirectory( AbstractReleaseTestCase.getPath( file.getParentFile() ) );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testWriteToNewFileRequiredOnly()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/new-release.properties" );
        file.delete();
        assertFalse( "Check file doesn't exist", file.exists() );

        ReleaseDescriptor config = new ReleaseDescriptor();
        config.setCompletedPhase( "completed-phase-write" );
        config.setScmSourceUrl( "url-write" );

        store.write( config, file );

        ReleaseDescriptor rereadDescriptor = store.read( file );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testWriteToNewFileDottedIds()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/new-release.properties" );
        file.delete();
        assertFalse( "Check file doesn't exist", file.exists() );

        ReleaseDescriptor config = new ReleaseDescriptor();
        config.setCompletedPhase( "completed-phase-write" );
        config.setScmSourceUrl( "url-write" );

        config.mapReleaseVersion( "group.id:artifact.id", "1.1" );
        config.mapDevelopmentVersion( "group.id:artifact.id", "1.2-SNAPSHOT" );

        IdentifiedScm scm = new IdentifiedScm();
        scm.setId( "id" );
        scm.setConnection( "connection" );
        scm.setDeveloperConnection( "devConnection" );
        scm.setTag( "tag" );
        scm.setUrl( "url" );
        config.mapOriginalScmInfo( "group.id:artifact.id", scm );

        store.write( config, file );

        ReleaseDescriptor rereadDescriptor = store.read( file );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testWriteToNewFileNullMappedScm()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/new-release.properties" );
        file.delete();
        assertFalse( "Check file doesn't exist", file.exists() );

        ReleaseDescriptor config = new ReleaseDescriptor();
        config.setCompletedPhase( "completed-phase-write" );
        config.setScmSourceUrl( "url-write" );

        config.mapReleaseVersion( "group.id:artifact.id", "1.1" );
        config.mapDevelopmentVersion( "group.id:artifact.id", "1.2-SNAPSHOT" );

        config.mapOriginalScmInfo( "group.id:artifact.id", null );

        store.write( config, file );

        ReleaseDescriptor rereadDescriptor = store.read( file );

        assertNull( "check null scm is mapped correctly",
                    rereadDescriptor.getOriginalScmInfo().get( "group.id:artifact.id" ) );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testOverwriteFile()
        throws ReleaseDescriptorStoreException
    {
        File file = getTestFile( "target/test-classes/rewrite-release.properties" );
        assertTrue( "Check file already exists", file.exists() );

        ReleaseDescriptor config = createReleaseConfigurationForWriting();

        store.write( config, file );

        ReleaseDescriptor rereadDescriptor = store.read( file );

        assertEquals( "compare configuration", config, rereadDescriptor );
    }

    public void testDeleteFile()
        throws ReleaseDescriptorStoreException, IOException
    {
        File file = getTestFile( "target/test-classes/delete/release.properties" );
        file.getParentFile().mkdirs();
        file.createNewFile();
        assertTrue( "Check file already exists", file.exists() );

        ReleaseDescriptor config = createReleaseConfigurationForWriting();
        config.setWorkingDirectory( AbstractReleaseTestCase.getPath( file.getParentFile() ) );

        store.delete( config );

        assertFalse( "Check file already exists", file.exists() );
    }

    public void testMissingDeleteFile()
        throws ReleaseDescriptorStoreException, IOException
    {
        File file = getTestFile( "target/test-classes/delete/release.properties" );
        file.getParentFile().mkdirs();
        file.delete();
        assertFalse( "Check file already exists", file.exists() );

        ReleaseDescriptor config = createReleaseConfigurationForWriting();
        config.setWorkingDirectory( AbstractReleaseTestCase.getPath( file.getParentFile() ) );

        store.delete( config );

        assertFalse( "Check file already exists", file.exists() );
    }

    private ReleaseDescriptor createReleaseConfigurationForWriting()
    {
        ReleaseDescriptor config = new ReleaseDescriptor();
        config.setCompletedPhase( "completed-phase-write" );
        config.setScmSourceUrl( "url-write" );
        config.setScmId( "id-write" );
        config.setScmUsername( "username-write" );
        config.setScmPassword( "password-write" );
        config.setScmPrivateKey( "private-key-write" );
        config.setScmPrivateKeyPassPhrase( "passphrase-write" );
        config.setScmTagBase( "tag-base-write" );
        config.setScmBranchBase( "branch-base-write" );
        config.setScmReleaseLabel( "tag-write" );
        config.setAdditionalArguments( "additional-args-write" );
        config.setPreparationGoals( "preparation-goals-write" );
        config.setCompletionGoals( "completion-goals-write" );
        config.setPomFileName( "pom-file-name-write" );

        config.mapReleaseVersion( "groupId:artifactId", "1.0" );
        config.mapDevelopmentVersion( "groupId:artifactId", "1.1-SNAPSHOT" );

        IdentifiedScm scm = new IdentifiedScm();
        scm.setId( "id-write" );
        scm.setConnection( "connection-write" );
        scm.setDeveloperConnection( "developerConnection-write" );
        scm.setUrl( "url-write" );
        scm.setTag( "tag-write" );
        config.mapOriginalScmInfo( "groupId:artifactId", scm );

        scm = new IdentifiedScm();
        scm.setConnection( "connection-write" );
        // omit optional elements
        config.mapOriginalScmInfo( "groupId:subproject1", scm );

        return config;
    }

    private static void assertDefaultReleaseConfiguration( ReleaseDescriptor config )
    {
        assertNull( "Expected no completedPhase", config.getCompletedPhase() );
        assertNull( "Expected no id", config.getScmId() );
        assertNull( "Expected no url", config.getScmSourceUrl() );
        assertNull( "Expected no username", config.getScmUsername() );
        assertNull( "Expected no password", config.getScmPassword() );
        assertNull( "Expected no privateKey", config.getScmPrivateKey() );
        assertNull( "Expected no passphrase", config.getScmPrivateKeyPassPhrase() );
        assertNull( "Expected no tagBase", config.getScmTagBase() );
        assertNull( "Expected no tag", config.getScmReleaseLabel() );
        assertNull( "Expected no additional arguments", config.getAdditionalArguments() );
        assertNull( "Expected no preparation goals", config.getPreparationGoals() );
        assertNull( "Expected no completion goals", config.getCompletionGoals() );
        assertNull( "Expected no pom file name", config.getPomFileName() );

        assertNull( "Expected no workingDirectory", config.getWorkingDirectory() );
        assertFalse( "Expected no generateReleasePoms", config.isGenerateReleasePoms() );
        assertFalse( "Expected no useEditMode", config.isScmUseEditMode() );
        assertTrue( "Expected default interactive", config.isInteractive() );
        assertFalse( "Expected no addScema", config.isAddSchema() );

        assertTrue( "Expected no release version mappings", config.getReleaseVersions().isEmpty() );
        assertTrue( "Expected no dev version mappings", config.getDevelopmentVersions().isEmpty() );
        assertTrue( "Expected no scm mappings", config.getOriginalScmInfo().isEmpty() );
        assertNotNull( "Expected resolved snapshot dependencies map", config.getResolvedSnapshotDependencies() );
    }

    public ReleaseDescriptor createMergeConfiguration()
        throws IOException
    {
        ReleaseDescriptor releaseDescriptor = new ReleaseDescriptor();

        releaseDescriptor.setScmSourceUrl( "scm-url" );
        releaseDescriptor.setScmUsername( "username" );
        // Not setting other optional SCM settings for brevity
        releaseDescriptor.setWorkingDirectory( AbstractReleaseTestCase.getPath( getTestFile( "target/test-working-directory" ) ) );
        // Not setting non-override setting completedPhase

        return releaseDescriptor;
    }

    private ReleaseDescriptor createExpectedReleaseConfiguration()
    {
        ReleaseDescriptor expected = new ReleaseDescriptor();
        expected.setCompletedPhase( "step1" );
        expected.setScmId( "scm-id" );
        expected.setScmSourceUrl( "scm-url" );
        expected.setScmUsername( "username" );
        expected.setScmPassword( "password" );
        expected.setScmPrivateKey( "private-key" );
        expected.setScmPrivateKeyPassPhrase( "passphrase" );
        expected.setScmTagBase( "tagBase" );
        expected.setScmTagNameFormat( "expectedTagNameFormat" );
        expected.setScmBranchBase( "branchBase" );
        expected.setScmReleaseLabel( "tag" );
        expected.setAdditionalArguments( "additional-arguments" );
        expected.setPreparationGoals( "preparation-goals" );
        expected.setCompletionGoals( "completion-goals" );
        expected.setPomFileName( "pom-file-name" );
        expected.setWorkingDirectory( null );
        expected.setGenerateReleasePoms( false );
        expected.setScmUseEditMode( false );
        expected.setInteractive( true );
        expected.setAddSchema( false );
        expected.mapReleaseVersion( "groupId:artifactId1", "2.0" );
        expected.mapReleaseVersion( "groupId:artifactId2", "3.0" );
        expected.mapDevelopmentVersion( "groupId:artifactId1", "2.1-SNAPSHOT" );
        expected.mapDevelopmentVersion( "groupId:artifactId2", "3.0.1-SNAPSHOT" );
        IdentifiedScm scm = new IdentifiedScm();
        scm.setId( "id" );
        scm.setConnection( "connection" );
        scm.setDeveloperConnection( "developerConnection" );
        scm.setUrl( "url" );
        scm.setTag( "tag" );
        expected.mapOriginalScmInfo( "groupId:artifactId1", scm );
        scm = new IdentifiedScm();
        scm.setId( null );
        scm.setConnection( "connection2" );
        scm.setUrl( "url2" );
        scm.setTag( null );
        scm.setDeveloperConnection( null );
        expected.mapOriginalScmInfo( "groupId:artifactId2", scm );
        expected.mapResolvedSnapshotDependencies( "external:artifactId", "1.0", "1.1-SNAPSHOT" );

        return expected;
    }

}
