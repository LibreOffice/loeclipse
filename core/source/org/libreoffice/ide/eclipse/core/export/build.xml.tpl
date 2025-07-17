<?xml version="1.0" encoding="UTF-8"?>
<project name="{0}" default="package-oxt" basedir="." xmlns:if="ant:if" xmlns:unless="ant:unless">

    <!--
        ************************************************************
        In order to run this script, you need to set the path of the
        LibreOffice & SDK installation in the build.properties file
        ************************************************************
    -->

    <fail message="Please build using Ant 1.10.0 or higher.">
        <condition>
            <not>
                <antversion atleast="1.10.0"/>
            </not>
        </condition>
    </fail>

    <target name="init-env">

        <!-- set properties from Libreoffice installation and its SDK -->
        <property file="$'{'ant.file}/../build.properties"/>

        <path id="workspace.dir" location=".."/>
        <pathconvert property="workspace.path" refid="workspace.dir"/>

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
        <echo message="Workspace path: $'{'workspace.path}"/>

        <property name="office.unotypes.rdb" value="$'{'office.libs.path}$'{'file.separator}types.rdb"/>

        <first id="offapirdb">
            <fileset dir="$'{'office.install.dir}" includes="**/offapi.rdb"/>
        </first>
        <property name="office.offapi.rdb" value="$'{'toString:offapirdb}"/>

        <property name="sdk.idl.dir" location="$'{'sdk.dir}$'{'file.separator}idl"/>
        <property name="sdk.bin.dir" location="$'{'sdk.dir}$'{'file.separator}bin"/>

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
        <property name="idl.rdb.fullpath" value="$'{'idl.out.rdb}$'{'file.separator}types.rdb"/>
        <property name="build.classes.dir" value="$'{'build.dir}$'{'file.separator}classes"/>

        <!-- create a few temporary files -->
        <property name="project.mimetype" value="mimetype"/>
        <property name="project.files" value="packagefiles.txt"/>
        <property name="project.zip" value="packagefiles.zip"/>

        <!-- delete temporary files if present -->
        <delete file="$'{'project.mimetype}"/>
        <delete file="$'{'project.files}"/>
        <delete file="$'{'project.zip}"/>

        <!-- create the build and dist directory if not exist -->
        <mkdir dir="$'{'build.dir}"/>
        <mkdir dir="$'{'dist.dir}"/>

        <!-- clean the build and dist directory without deleting it -->
        <delete includeemptydirs="true">
            <fileset dir="$'{'build.dir}" includes="**/idl/,**/classes/"/>
        </delete>
        <delete includeemptydirs="true">
            <fileset dir="$'{'dist.dir}" includes="**/*"/>
        </delete>

        <!-- create a few empty directories -->
        <mkdir dir="$'{'idl.out}"/>
        <mkdir dir="$'{'idl.out.urd}"/>
        <mkdir dir="$'{'idl.out.rdb}"/>

        <first id="regview">
            <fileset dir="$'{'office.libs.path}" includes="**/regview,**/regview.exe"/>
        </first>
        <property name="sdk.regview" value="$'{'toString:regview}"/>

        <first id="idlr">
            <fileset dir="$'{'sdk.bin.dir}" includes="unoidl-read,unoidl-read.exe"/>
        </first>
        <property name="sdk.idlr" value="$'{'toString:idlr}"/>

        <first id="idlw">
            <fileset dir="$'{'sdk.bin.dir}" includes="unoidl-write,unoidl-write.exe"/>
        </first>
        <property name="sdk.idlw" value="$'{'toString:idlw}"/>

        <condition property="sdk.useidlw">
            <available file="$'{'sdk.idlw}" type="file"/>
        </condition>

        <condition property="sdk.useidlc">
            <not>
                <isset property="sdk.useidlw"/>
            </not>
        </condition>

        <echo if:set="sdk.useidlw" message="Project SDK use unoidl-write"/>
        <echo if:set="sdk.useidlc" message="Project SDK use idlc"/>
        <echo message="Project language $'{'project.language}"/>

        <!-- check if project is Java -->
        <condition property="project.isjava">
            <matches pattern="Java" string="$'{'project.language}"/>
        </condition>
    </target>

    <target name="init-java" depends="init-env" if="project.isjava">
        <echo message="Project is Java using version: $'{'ant.java.version}"/>

        <!-- Setting lib.dir dependencies default directory is /lib or /libs if exist -->
        <condition property="lib.dir" value="libs">
            <and>
                <not>
                    <available file="lib" type="dir"/>
                </not>
                <available file="libs" type="dir"/>
            </and>
        </condition>
        <condition property="lib.dir" value="lib">
            <available file="lib" type="dir"/>
        </condition>
        <property unless:set="uno.java.classpath" name="lib.dir" value="lib"/>

        <property name="project.jar" value="$'{'ant.project.name}.jar"/>
        <property name="build.jar" value="$'{'basedir}$'{'file.separator}$'{'project.jar}"/>

        <first id="module">
            <fileset dir="$'{'src.dir.absolute}" includes="module-info.java"/>
        </first>
        <property name="src.module" value="$'{'toString:module}"/>
        <condition property="src.withmodule">
            <and>
                <javaversion atleast="11"/>
                <available file="$'{'src.module}" type="file"/>
            </and>
        </condition>
        <property unless:set="src.withmodule" name="src.withoutmodule" value="true"/>
        
        <echo if:set="src.withmodule" message="Compiling jar with module"/>
        <echo if:set="src.withoutmodule" message="Compiling jar without module"/>

        <first id="javamaker">
            <fileset dir="$'{'sdk.bin.dir}" includes="javamaker,javamaker.exe"/>
        </first>
        <property name="sdk.javamaker" value="$'{'toString:javamaker}"/>

        <path id="office.class.path">
            <fileset dir="$'{'office.libs.path}$'{'file.separator}classes">
                <include name="*.jar"/>
            </fileset>
        </path>

        <path id="office.module.path">
            <fileset dir="$'{'office.libs.path}$'{'file.separator}classes">
                <include name="libreoffice.jar"/>
            </fileset>
        </path>

        <path id="build.lib.path">
            <filelist dir="$'{'workspace.path}" files="$'{'uno.java.classpath}"/>
        </path>

        <path if:set="lib.dir" id="build.class.path">
            <fileset dir="$'{'basedir}">
                <include name="$'{'lib.dir}$'{'file.separator}**$'{'file.separator}*.jar"/>
            </fileset>
        </path>
        <path unless:set="lib.dir" id="build.class.path" refid="build.lib.path"/>

        <pathconvert property="manifest.class.path" pathsep=" ">
            <path refid="build.class.path"/>
            <globmapper from="$'{'basedir}/*" to="*"/>
        </pathconvert>

        <!-- create a few empty directories -->
        <mkdir dir="$'{'build.classes.dir}"/>

        <echo message="Binary build path: $'{'toString:build.lib.path}"/>
        <echo message="Manifest class path: $'{'manifest.class.path}"/>
    </target>

    <target name="init-idlc" depends="init-java" if="sdk.useidlc">
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

    <target name="compile-idlc" depends="init-idlc" if="sdk.useidlc">
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

    <target name="merge-urd" depends="compile-idlc" if="sdk.useidlc">
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

    <target name="compile-idlwrite" depends="merge-urd" if="sdk.useidlw">
        <echo message="Compile tool: $'{'sdk.idlw}"/>
        <echo message="Office unotype: $'{'office.unotypes.rdb}"/>
        <echo message="Office offapi: $'{'office.offapi.rdb}"/>
        <echo message="idl dir: $'{'idl.dir}"/>
        <echo message="rdb dir: $'{'idl.rdb.fullpath}"/>

        <delete file="$'{'idl.rdb.fullpath}"/>
        <exec executable="$'{'sdk.idlw}" failonerror="true">
            <arg value="$'{'office.unotypes.rdb}"/>
            <arg value="$'{'office.offapi.rdb}"/>
            <arg value="$'{'idl.dir}"/>
            <arg value="$'{'idl.rdb.fullpath}"/>
        </exec>
    </target>

    <target name="show-idlc" depends="compile-idlwrite" if="sdk.useidlc">
        <echo message="Tool regview: $'{'sdk.regview}"/>
        <exec executable="$'{'sdk.regview}" failonerror="true">
            <arg value="$'{'idl.rdb.fullpath}"/>
        </exec>
    </target>

    <target name="show-idlw" depends="show-idlc" if="sdk.useidlw">
        <echo message="Tool unoidl-read: $'{'sdk.idlr}"/>
        <exec executable="$'{'sdk.idlr}" failonerror="true">
            <arg value="$'{'office.unotypes.rdb}"/>
            <arg value="$'{'office.offapi.rdb}"/>
            <arg value="$'{'idl.rdb.fullpath}"/>
        </exec>
    </target>

    <target name="types" depends="show-idlw" if="project.isjava">
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

    <target name="compile-java" depends="types" if="src.withoutmodule">
        <echo message="Build class path: $'{'build.classes.dir}"/>
        <echo message="Source dir: $'{'src.dir.absolute}"/>
        <javac srcdir="$'{'src.dir.absolute}" source="1.8" target="1.8" encoding="UTF-8"
               includeantruntime="false" debug="true" debuglevel="lines,vars,source"
               destdir="$'{'build.classes.dir}" excludes="**/module-info.java,**/*Test*">
            <classpath>
                <pathelement location="$'{'build.classes.dir}"/>
                <path refid="build.class.path"/>
                <path refid="build.lib.path"/>
                <path refid="office.class.path"/>
            </classpath>
        </javac>
    </target>

    <target name="compile-module" depends="compile-java" if="src.withmodule">
        <echo message="Build module path: $'{'build.classes.dir}"/>
        <echo message="Source dir: $'{'src.dir.absolute}"/>
        <javac srcdir="$'{'src.dir.absolute}" source="11" target="11" encoding="UTF-8"
               includeantruntime="false" debug="true" debuglevel="lines,vars,source"
               destdir="$'{'build.classes.dir}" excludes="**/*Test*">
            <modulepath>
                <pathelement location="$'{'build.classes.dir}"/>
                <path refid="build.class.path"/>
                <path refid="build.lib.path"/>
                <path refid="office.module.path"/>
            </modulepath>
        </javac>
    </target>

    <target name="package-jar" depends="compile-module" if="project.isjava">
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

    <target name="show-module" depends="package-jar" if="src.withmodule">
        <echo message="Java module in archive: $'{'project.jar}"/>
        <exec executable="jar">
            <arg value="-f"/>
            <arg value="$'{'build.jar}"/>
            <arg value="-d"/>
        </exec>
    </target>

    <target name="package-zip" depends="show-module">
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

</project>
