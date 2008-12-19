/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer.detailPanel.internal.form.object;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.qi4j.library.swing.visualizer.model.descriptor.CompositeDetailDescriptor;
import org.qi4j.library.swing.visualizer.model.descriptor.MixinDetailDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import org.qi4j.spi.mixin.MixinDescriptor;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class MixinDescriptorForm
{
    private JComponent mixinSeparator;
    private JTextField mixinClassName;
    private JTextField compositeClassName;

    private JPanel mixinPanel;

    public MixinDescriptorForm()
    {
        $$$setupUI$$$();
    }

    public final void updateModel( MixinDetailDescriptor aDescriptor )
    {
        String mixinClassNameStr = null;
        String compositeClassNameStr = null;

        if( aDescriptor != null )
        {
            MixinDescriptor descriptor = aDescriptor.descriptor();
            mixinClassNameStr = descriptor.mixinClass().getName();

            CompositeDetailDescriptor compositeDDesc = aDescriptor.composite();
            CompositeDescriptor compositeDesc = compositeDDesc.descriptor();
            compositeClassNameStr = compositeDesc.type().getName();
        }

        mixinClassName.setText( mixinClassNameStr );
        compositeClassName.setText( compositeClassNameStr );
    }


    private void createUIComponents()
    {
        DefaultComponentFactory cmpFactory = DefaultComponentFactory.getInstance();
        mixinSeparator = cmpFactory.createSeparator( "Mixin" );
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        mixinPanel = new JPanel();
        mixinPanel.setLayout( new FormLayout( "fill:max(d;4px):noGrow,fill:p:noGrow,left:4dlu:noGrow,fill:max(p;75dlu):noGrow,left:4dlu:noGrow,left:d:grow,fill:max(d;4px):noGrow", "center:4px:noGrow,center:p:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow" ) );
        ( (FormLayout) mixinPanel.getLayout() ).setRowGroups( new int[][]{ new int[]{ 4, 6 }, new int[]{ 1, 7 } } );
        ( (FormLayout) mixinPanel.getLayout() ).setColumnGroups( new int[][]{ new int[]{ 3, 5 }, new int[]{ 1, 7 } } );
        CellConstraints cc = new CellConstraints();
        mixinPanel.add( mixinSeparator, cc.xyw( 2, 2, 5 ) );
        final JLabel label1 = new JLabel();
        label1.setText( "Class Name" );
        mixinPanel.add( label1, cc.xy( 2, 4 ) );
        final JLabel label2 = new JLabel();
        label2.setText( "Composite" );
        mixinPanel.add( label2, cc.xy( 2, 6 ) );
        mixinClassName = new JTextField();
        mixinClassName.setEditable( false );
        mixinClassName.setText( "" );
        mixinPanel.add( mixinClassName, cc.xy( 4, 4 ) );
        compositeClassName = new JTextField();
        compositeClassName.setEditable( false );
        compositeClassName.setText( "" );
        mixinPanel.add( compositeClassName, cc.xy( 4, 6 ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return mixinPanel;
    }
}
