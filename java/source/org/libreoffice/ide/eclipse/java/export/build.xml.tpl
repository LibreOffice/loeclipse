<?xml version="1.0" encoding="UTF-8"?>
<project name="{0}" default="package-oxt" basedir=".">

    <target name="init-env">
        
        <!--  set properties from libreoffice install and its SDK -->
        <property file="$'{'ant.file}/../build.properties"/>
        
        <echo message="Initializing the properties for LibreOffice"/>
        <first id="sofficeParents">
            <dirset dir="$'{'office.install.dir}">
                <present targetdir="$'{'office.install.dir}">
                    <mapper type="glob" from="*" to="*/soffice.bin" />
                </present>
            </dirset>
        </first>
        <property name="office.basis.dir" value="$'{'toString:sofficeParents}$'{'file.separator}.."/>
        <property name="office.ure.dir" value="$'{'office.basis.dir}$'{'file.separator}ure-link"/>

        <property name="office.libs.path" value="$'{'office.install.dir}$'{'file.separator}program$'{'path.separator}$'{'office.basis.dir}$'{'file.separator}program$'{'path.separator}$'{'office.ure.dir}$'{'file.separator}lib"/>

        <echo message="Office libs path: $'{'office.libs.path}"/>

        <path id="office.class.path">
            <fileset dir="$'{'office.basis.dir}$'{'file.separator}program$'{'file.separator}classes">
                <include name="*.jar"/>
            </fileset>
        </path>

        <property name="office.unotypes.rdb" 
            value="$'{'office.basis.dir}$'{'file.separator}program$'{'file.separator}types.rdb"/>

        <first id="offapirdbPaths">
            <fileset dir="$'{'office.install.dir}" includes="**/offapi.rdb" />
        </first>
        <property name="office.offapi.rdb" value="$'{'toString:offapirdbPaths}"/>

        <property name="sdk.idl.dir" location="$'{'sdk.dir}$'{'file.separator}idl"/> 
        <property name="sdk.idlc" value="$'{'sdk.dir}$'{'file.separator}$'{'sdk.bin.dir}$'{'file.separator}idlc"/>
        <property name="sdk.javamaker" value="$'{'sdk.dir}$'{'file.separator}$'{'sdk.bin.dir}$'{'file.separator}javamaker"/>

        <first id="regmergePaths">
            <union>
                <fileset dir="$'{'office.ure.dir}" includes="**/regmerge" />
                <fileset dir="$'{'sdk.dir}" includes="**/regmerge" />
            </union>
        </first>
        <property name="sdk.regmerge" value="$'{'toString:regmergePaths}"/>

        <property environment="env"/>
        <property name="office.tool.path" value="$'{'env.PATH}$'{'path.separator}$'{'office.libs.path}"/>

        <!-- set properties for the project structure -->
        <property file=".unoproject"/>
        <property file="package.properties"/>
        <dirname property="project.dir" file="package.properties"/>

        <property name="build.dir" value="$'{'project.build}"/>
        <property name="build.classes.dir" value="$'{'build.dir}/classes"/>
        <property name="dist.dir" value="$'{'build.dir}/dist"/>

        <!-- set properties for the output structure -->

        <property name="uno.package.name" value="$'{'project.dir}$'{'file.separator}$'{'dist.dir}$'{'file.separator}$'{'ant.project.name}.oxt"/>
        <property name="src.dir.absolute" value="$'{'project.dir}$'{'project.srcdir}"/>
        <property name="idl.dir" value="$'{'project.dir}$'{'project.idl}"/>

        <property name="idl.out" value="$'{'project.dir}$'{'file.separator}$'{'build.dir}$'{'file.separator}idl"/>
        <property name="idl.out.urd" value="$'{'idl.out}$'{'file.separator}urd"/>
        <property name="idl.out.rdb" value="$'{'idl.out}$'{'file.separator}rdb"/>
        <property name="idl.rdb.name" value="types.rdb"/>
        <property name="idl.rdb.fullpath" value="$'{'idl.out.rdb}$'{'file.separator}$'{'idl.rdb.name}"/>

        <!-- create a few empty directories -->

        <mkdir dir="$'{'build.dir}" />
        <mkdir dir="$'{'idl.out}"/>
        <mkdir dir="$'{'idl.out.urd}"/>
        <mkdir dir="$'{'idl.out.rdb}"/>
        <mkdir dir="$'{'build.classes.dir}"/>
        <mkdir dir="$'{'dist.dir}"/>
    </target>

    <target name="clean" depends="init-env">
        <delete dir="$'{'build.dir}"/>
    </target>

    <target name="package-oxt" depends="package-jar">
        <zip destfile="$'{'uno.package.name}" includes="$'{'contents}">
            <fileset dir="." includes="$'{'contents}"/>
            <zipfileset dir="descriptions" includes="**/*.txt" prefix="descriptions"/>
            <zipfileset file="ant/manifest.xml" fullpath="META-INF/manifest.xml"/>
            <zipfileset file="$'{'dist.dir}/$'{'ant.project.name}.jar" 
                fullpath="$'{'ant.project.name}.jar"/>
            <zipfileset file="$'{'idl.rdb.fullpath}" fullpath="types.rdb"/>
        </zip>
    </target>

    <target name="package-jar" depends="compile-java">
        <jar destfile="$'{'dist.dir}/$'{'ant.project.name}.jar">
            <manifest>
                <attribute name="RegistrationClassName" value="$'{'regclassname}"/>
            </manifest>
            <fileset dir="$'{'build.classes.dir}">
                <include name="**/*.class"/>
            </fileset>
            <fileset dir="$'{'src.dir.absolute}">
                <exclude name="**/*.java"/>
            </fileset>
        </jar>
    </target>

    <target name="compile-java" depends="types">
        <echo message="build classes: $'{'build.classes.dir}"/>
        <javac srcdir="$'{'src.dir.absolute}" source="1.4" target="1.4" encoding="UTF-8"
            destdir="$'{'build.classes.dir}" excludes="**/*Test*">
            <classpath>
                <pathelement location="$'{'build.classes.dir}"/>
                <path refid="office.class.path"/>
            </classpath>
        </javac>
    </target>

    <target name="types-old" depends="merge-urd">
        <echo message="Generating java class files from rdb..."/>
        <condition property="args.offapi" value=" -X$'{'office.offapi.rdb}" else="">
            <length string="$'{'office.offapi.rdb}" when="greater" length="0" trim="true"/>
        </condition>

        <exec executable="$'{'sdk.javamaker}" errorproperty="regmerge.err"
              resultproperty="regmerge.result">
            <env key="PATH" path="$'{'office.tool.path}"/>    
            <env key="LD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <env key="DYLD_LIBRARY_PATH" path="$'{'office.tool.path}"/>
            <arg value="-nD"/>
            <arg value="-Gc"/>
            <arg value="-BUCR"/>
            <arg value="-O"/>
            <arg value="$'{'project.dir}$'{'file.separator}$'{'build.classes.dir}"/>
            <arg file="$'{'idl.rdb.fullpath}"/>
            <arg line="-X$'{'office.unotypes.rdb}$'{'args.offapi}"/>
        </exec>

        <condition property="regmerge.noprefix" value="true" else="false">
            <contains string="$'{'regmerge.err}" substring="-BUCR"/>
        </condition>

        <condition property="regermerge.failed">
            <and>
                <isfalse value="$'{'regmerge.noprefix}"/>
                <isfailure code="$'{'regmerge.result}"/>
            </and>
        </condition>

        <fail if="$'{'regmerge.failed}" message="Regmerge failed: $'{'regmerge.err}"/>
    </target>

    <target name="types-noprefix" depends="types-old" if="$'{'regmerge.noprefix}">
        <condition property="args.offapi" value=" -X$'{'office.offapi.rdb}" else="">
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
            <arg line="-X$'{'office.unotypes.rdb}$'{'args.offapi}"/>
        </exec>
    </target>

    <target name="types" depends="types-old, types-noprefix" />

	<target name="merge-urd" depends="compile-idl">
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

    <target name="compile-idl" depends="init-env">
        <echo message="$'{'sdk.idlc}"/>
        <echo message="$'{'office.tool.path}"/>

        <apply executable="$'{'sdk.idlc}" failonerror="true">
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
