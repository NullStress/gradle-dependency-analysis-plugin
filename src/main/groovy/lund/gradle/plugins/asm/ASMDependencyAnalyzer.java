package lund.gradle.plugins.asm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.objectweb.asm.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: Kristian
 * Date: 10.07.14
 * Time: 20:55
 */
public class ASMDependencyAnalyzer extends ClassVisitor implements Opcodes {

    private Set<String> classes;

    ASMDependencyAnalyzer() {
        super(ASM4);
        classes = new HashSet<String>();
    }


    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
//        System.out.println(name + " extends " + superName + " {");
        if ( signature == null )
        {
            addName( superName );
            addNames( interfaces );
        }
        else
        {
            addSignature( signature );
        }
    }
    public void visitSource(String source, String debug) {
    }
    public void visitOuterClass(String owner, String name, String desc) {
    }
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        addDesc( desc );
        return null;
    }

    @Override
    public void visitAttribute(Attribute attribute) {

    }

    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
    }
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if ( signature == null )
        {
            addDesc( desc );
        }
        else
        {
            addTypeSignature( signature );
        }

        if ( value instanceof Type )
        {
            addType( (Type) value );
        }
        return null;
    }
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        if ( signature == null )
        {
            addMethodDesc( desc );
        }
        else
        {
            addSignature( signature );
        }

        addNames( exceptions );
        return null;
    }
    public void visitEnd() {
    }

    public Set<String> getClasses() {
        return classes;
    }

    // private methods --------------------------------------------------------

    private void addName( String name )
    {
        if ( name == null )
        {
            return;
        }

        // decode arrays
        if ( name.startsWith( "[L" ) && name.endsWith( ";" ) )
        {
            name = name.substring( 2, name.length() - 1 );
        }

        // decode internal representation
        name = name.replace( '/', '.' );

        classes.add( name );
    }

    private void addNames( final String[] names )
    {
        if ( names == null )
        {
            return;
        }

        for ( String name : names )
        {
            addName( name );
        }
    }

    private void addDesc( final String desc )
    {
        addType( Type.getType( desc ) );
    }

    private void addMethodDesc( final String desc )
    {
        addType( Type.getReturnType( desc ) );

        Type[] types = Type.getArgumentTypes( desc );

        for ( Type type : types )
        {
            addType( type );
        }
    }

    private void addType( final Type t )
    {
        switch ( t.getSort() )
        {
            case Type.ARRAY:
                addType( t.getElementType() );
                break;

            case Type.OBJECT:
                addName( t.getClassName().replace( '.', '/' ) );
                break;
        }
    }

    private void addSignature( final String signature )
    {
        if ( signature != null )
        {
//            new SignatureReader( signature ).accept( this );
        }
    }

    private void addTypeSignature( final String signature )
    {
        if ( signature != null )
        {
//            new SignatureReader( signature ).acceptType( this );
        }
    }

}
