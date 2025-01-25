<?xml version="1.0" encoding="UTF-8"?>
<project name="{0}" default="package-oxt" basedir="." xmlns:if="ant:if" xmlns:unless="ant:unless">

    <!--
        ************************************************************
        In order to run this script, you need to set the path of the
        LibreOffice & SDK installation in the build.properties file
        ************************************************************
    -->

    <target name="init-env">

        <!-- set properties from Libreoffice installation and its SDK -->
        <property file="$'{'ant.file}/../build.properties"/>

        <echo message="Initializing the properties for LibreOffice"/>
        <first id="sofficeParents">
            <dirset dir="$'{'office.install.dir}">
                <present targetdir="$'{'office.install.dir}">
                    <mapper type="glob" from="*" to="*/soffice.bin"/>
                </present>
            </dirset>
        </first>
        <property name="office.libs.path" value="$'{'office.install.dir}$'{'file.separator}program"/>

        <echo message="Office libs path: $'{'office.libs.path}"/>

        <property name="office.unotypes.rdb" value="$'{'office.libs.path}$'{'file.separator}types.rdb"/>

        <first id="offapirdb">
            <fileset dir="$'{'office.install.dir}" includes="**/offapi.rdb"/>
        </first>
        <property name="office.offapi.rdb" value="$'{'toString:offapirdb}"/>

        <property name="sdk.idl.dir" location="$'{'sdk.dir}$'{'file.separator}idl"/>
        <property name="sdk.bin.dir" location="$'{'sdk.dir}$'{'file.separator}bin"/>

        <first id="idlw">
            <fileset dir="$'{'sdk.bin.dir}" includes="unoidl-write,unoidl_write.exe"/>
        </first>
        <property name="sdk.idlw" value="$'{'toString:idlw}"/>
        <echo message="sdk.idlw: $'{'sdk.idlw}"/>
        <available file="$'{'sdk.idlw}" property="sdk.useidlw" type="file"/>

        <first id="javamaker">
            <fileset dir="$'{'sdk.bin.dir}" includes="javamaker,javamaker.exe"/>
        </first>
        <property name="sdk.javamaker" value="$'{'toString:javamaker}"/>

        <property environment="env"/>
        <property name="office.tool.path" value="$'{'env.PATH}$'{'path.separator}$'{'office.libs.path}"/>

        <!-- set properties for the project structure -->
        <property file=".unoproject"/>
        <dirname property="project.dir" file="package.properties"/>

        <property name="build.dir" value="$'{'project.build}"/>
        <property name="dist.dir" value="dist"/>

        <!-- set properties for the output structure -->
        <property name="uno.package.name" value="$'{'project.dir}$'{'file.separator}$'{'dist.dir}$'{'file.separator}$'{'ant.project.name}.oxt"/>
        <property name="src.dir.absolute" value="$'{'project.dir}$'{'project.srcdir}"/>
        <property name="idl.dir" value="$'{'project.dir}$'{'project.idl}"/>

        <property name="idl.out" value="$'{'project.dir}$'{'file.separator}$'{'build.dir}$'{'file.separator}idl"/>
        <property name="idl.out.urd" value="$'{'idl.out}$'{'file.separator}urd"/>
        <property name="idl.out.rdb" value="$'{'idl.out}$'{'file.separator}rdb"/>
        <property name="idl.rdb.name" value="types.rdb"/>
        <property name="idl.rdb.fullpath" value="$'{'idl.out.rdb}$'{'file.separator}$'{'idl.rdb.name}"/>
        <property name="build.classes.dir" value="$'{'build.dir}$'{'file.separator}classes"/>

        <!-- create a few temporary files -->
        <property name="project.mimetype" value="mimetype"/>
        <property name="project.files" value="packagefiles.txt"/>
        <property name="project.zip" value="packagefiles.zip"/>

        <!-- clean buid and dist directory -->
        <delete dir="$'{'build.dir}"/>
        <delete dir="$'{'dist.dir}"/>

        <!-- create a few empty directories -->
        <mkdir dir="$'{'build.dir}"/>
        <mkdir dir="$'{'idl.out}"/>
        <mkdir dir="$'{'idl.out.urd}"/>
        <mkdir dir="$'{'idl.out.rdb}"/>
        <mkdir dir="$'{'dist.dir}"/>

        <echo if:set="sdk.useidlw" message="Project SDK use unoidl-write"/>
        <echo unless:set="sdk.useidlw" message="Project SDK use idlc"/>

        <!-- check if project is Java -->
        <available file="lib" property="project.isjava" type="dir"/>
        <echo if:set="project.isjava" message="Project is Java"/>
        <echo unless:set="project.isjava" message="Project is not Java"/>
    </target>

    <target name="init-idlc" depends="init-env" unless="sdk.useidlw">
        <first id="idlc">
            <fileset dir="$'{'sdk.bin.dir}" includes="idlc,idlc.exe"/>
        </first>
        <property name="sdk.idlc" value="$'{'toString:idlc}"/>
        <first id="regmerge">
            <union>
                <fileset dir="$'{'office.libs.path}" includes="**/regmerge,**/regmerge.exe"/>
                <fileset dir="$'{'sdk.dir}" includes="**/regmerge,**/regmerge.exe"/>
            </union>
        </first>
        <property name="sdk.regmerge" value="$'{'toString:regmerge}"/>
    </target>

    <target name="init-java" depends="init-idlc" if="project.isjava">

        <property name="lib.dir" value="lib"/>
        <property name="project.jar" value="$'{'ant.project.name}.jar"/>
        <property name="build.jar" value="$'{'build.dir}$'{'file.separator}$'{'project.jar}"/>

        <property name="lib.dir" value="lib"/>
        <property name="project.jar" value="$'{'ant.project.name}.jar"/>
        <property name="build.jar" value="$'{'build.dir}$'{'file.separator}$'{'project.jar}"/>

        <path id="office.class.path">
            <fileset dir="$'{'office.libs.path}$'{'file.separator}classes">
                <include name="*.jar"/>
            </fileset>
        </path>

        <path id="build.class.path">
            <fileset dir="$'{'basedir}">
                <include name="$'{'lib.dir}$'{'file.separator}*.jar"/>
            </fileset>
        </path>

        <pathconvert property="manifest.class.path" pathsep=" ">
            <path refid="build.class.path"/>
            <mapper>
                <chainedmapper>
                    <flattenmapper/>
                    <globmapper from="*.jar" to="$'{'lib.dir}/*.jar"/>
                </chainedmapper>
            </mapper>
        </pathconvert>

        <!-- create a few empty directories -->
        <mkdir dir="$'{'build.classes.dir}"/>

        <echo message="Manifest Class-Path: $'{'manifest.class.path}"/>
    </target>

    <target name="package-zip" depends="package-jar">
        <loadproperties srcFile="package.properties">
            <filterchain>
                <tokenfilter>
                    <replacestring from=", " to="\n"/>
                </tokenfilter>
            </filterchain>
        </loadproperties>
        <echo file="$'{'project.files}" append="false">$'{'contents}</echo>
        <zip destfile="$'{'project.zip}">
            <fileset dir="." includesfile="$'{'project.files}"/>
            <zipfileset dir="description" includes="**/*.txt" prefix="description"/>
            <zipfileset file="manifest.xml" fullpath="META-INF/manifest.xml"/>
            <zipfileset file="$'{'build.jar}" fullpath="$'{'project.jar}"/>
            <zipfileset file="$'{'idl.rdb.fullpath}" fullpath="types.rdb"/>
        </zip>
        <delete file="$'{'project.files}"/>
    </target>

    <target name="package-oxt" depends="package-zip">
        <echo file="$'{'project.mimetype}" append="false">application/vnd.openofficeorg.extension</echo>
        <zip destfile="$'{'uno.package.name}" compress="false" keepcompression="true">
            <zipfileset file="$'{'project.mimetype}"/>
            <zipfileset src="$'{'project.zip}"/>
        </zip>
        <delete file="$'{'project.mimetype}"/>
        <delete file="$'{'project.zip}"/>
    </target>

    <target name="package-jar" depends="compile-java" if="project.isjava">
        <echo message="Build Java archive: $'{'project.jar}"/>

        <jar destfile="$'{'build.jar}">
            <manifest>
                <attribute name="RegistrationClassName" value="$'{'regclassname}"/>
                <attribute name="Class-Path" value="$'{'manifest.class.path}"/>
            </manifest>
            <fileset dir="$'{'build.classes.dir}">
                <include name="**/*.class"/>
            </fileset>
            <fileset dir="$'{'src.dir.absolute}">
                <exclude name="**/*.java"/>
            </fileset>
        </jar>
    </target>

    <target name="compile-java" depends="types" if="project.isjava">
        <echo message="Build classes: $'{'build.classes.dir}"/>
        <javac srcdir="$'{'src.dir.absolute}" source="1.8" target="1.8" encoding="UTF-8"
            destdir="$'{'build.classes.dir}" excludes="**/*Test*">
            <classpath>
                <pathelement location="$'{'build.classes.dir}"/>
                <path refid="build.class.path"/>
                <path refid="office.class.path"/>
            </classpath>
        </javac>
    </target>

    <target name="types" depends="merge-urd">
        <condition property="args.offapi" value=" -X&quot;$'{'office.offapi.rdb}&quot;" else="">
            <length string="$'{'office.offapi.rdb}" when="greater" length="0" trim="true"/>
        </condition>

        <exec executable="$'{'sdk.javamaker}" failonerror="true">
            <env key="PATH" path="$'{'office.tool.path}"/>
            <env key="LD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <env key="DYLD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <arg value="-nD"/>
            <arg value="-Gc"/>
            <arg value="-O"/>
            <arg value="$'{'project.dir}$'{'file.separator}$'{'build.classes.dir}"/>
            <arg file="$'{'idl.rdb.fullpath}"/>
            <arg line="-X&quot;$'{'office.unotypes.rdb}&quot;"/>
            <arg line="$'{'args.offapi}"/>
        </exec>
    </target>

    <target name="merge-urd" depends="compile-idlc" unless="sdk.useidlw">
        <delete file="$'{'idl.rdb.fullpath}"/>
        <apply executable="$'{'sdk.regmerge}" failonerror="true">
            <env key="PATH" path="$'{'office.tool.path}"/>
            <env key="LD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <env key="DYLD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <arg file="$'{'idl.rdb.fullpath}"/>
            <arg value="/UCR"/>
            <fileset dir="$'{'idl.out.urd}" includes="**/*.urd" casesensitive="yes"/>
        </apply>
    </target>

    <target name="compile-idlwrite" depends="init-java" if="sdk.useidlw">
        <echo message="Compile tool: $'{'sdk.idlw}"/>

        <delete file="$'{'idl.rdb.fullpath}"/>
        <exec executable="$'{'sdk.idlw}" failonerror="true">
            <arg value="$'{'office.unotypes.rdb}"/>
            <arg value="$'{'office.offapi.rdb}"/>
            <arg value="$'{'idl.dir}"/>
            <arg value="$'{'idl.rdb.fullpath}"/>
        </exec>
    </target>

    <target name="compile-idlc" depends="compile-idlwrite" unless="sdk.useidlw">
        <echo message="Compile tool: $'{'sdk.idlc}"/>

        <apply executable="$'{'sdk.idlc}" failonerror="true" verbose="true">
            <env key="PATH" path="$'{'office.tool.path}"/>
            <env key="LD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <env key="DYLD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <arg value="-C"/>
            <arg value="-O"/>
            <arg value="$'{'idl.out.urd}"/>
            <arg value="-I"/>
            <arg value="$'{'idl.dir}"/>
            <arg value="-I"/>
            <arg value="$'{'sdk.idl.dir}"/>
            <fileset dir="$'{'idl.dir}" includes="**/*.idl" casesensitive="yes"/>
        </apply>
    </target>

</project>
