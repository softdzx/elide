package com.yahoo.elide.spring.config;

import com.google.common.collect.Sets;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.ElideTableToPojo;
import com.yahoo.elide.contrib.dynamicconfighelpers.parser.handlebars.HandlebarsHydrator;
import com.yahoo.elide.contrib.dynamicconfighelpers.model.ElideTable;
import org.mdkt.compiler.InMemoryJavaCompiler;

import java.util.Map;

public class EntityCompiler {
	
	
	public String dynamicConfigPath = "";

    public static String [] classNames = {
    		"com.yahoo.elide.contrib.dynamicconfig.model.PlayerStats"
			/*
			 * "example.models.ArtifactGroup", "example.models.ArtifactProduct",
			 * "example.models.ArtifactVersion"
			 */
    };

    private InMemoryJavaCompiler compiler = InMemoryJavaCompiler.newInstance()
            .useParentClassLoader(new InMemoryClassLoader(ClassLoader.getSystemClassLoader(), Sets.newHashSet(classNames)));

    private Map<String, Class<?>> compiledObjects;


	public String getElideTable(String path) {
		
		try {
			ElideTableToPojo securityPojo = new ElideTableToPojo();
			ElideTable table = securityPojo.parseTableConfigFile(path);
			HandlebarsHydrator obj = new HandlebarsHydrator();
			String tablePojo = obj.hydrateTableTemplate(table);
		    System.out.println(tablePojo);
		    
		    return tablePojo;
		} catch (Exception e ) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	    
		
		/*return "package example.models;\n" +
                "\n" +
                "import com.yahoo.elide.annotation.Include;\n" +
                "\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.OneToMany;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@Include(rootLevel = true, type = \"artifactGroup\")\n" +
                "@Entity\n" +
                "public class ArtifactGroup {\n" +
                "    @Id\n" +
                "    private String name = \"\";\n" +
                "\n" +
                "    private String commonName = \"\";\n" +
                "\n" +
                "    private String description = \"\";\n" +
                "\n" +
                "    @OneToMany(mappedBy = \"group\")\n" +
                "    private List<ArtifactProduct> products = new ArrayList<>();\n" +
                "}";*/
    }

    public String getArtifactProduct() {
        return "package example.models;\n" +
                "\n" +
                "import com.yahoo.elide.annotation.Include;\n" +
                "\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.ManyToOne;\n" +
                "import javax.persistence.OneToMany;\n" +
                "import java.util.ArrayList;\n" +
                "import java.util.List;\n" +
                "\n" +
                "@Include(type = \"artifactProduct\")\n" +
                "@Entity\n" +
                "public class ArtifactProduct {\n" +
                "    @Id\n" +
                "    private String name = \"\";\n" +
                "\n" +
                "    private String commonName = \"\";\n" +
                "\n" +
                "    private String description = \"\";\n" +
                "\n" +
                "    @ManyToOne\n" +
                "    private ArtifactGroup group = null;\n" +
                "\n" +
                "    @OneToMany(mappedBy = \"artifact\")\n" +
                "    private List<ArtifactVersion> versions = new ArrayList<>();\n" +
                "}";
    }

    public String getArtifactVersion() {
        return "package example.models;\n" +
                "\n" +
                "import com.yahoo.elide.annotation.Include;\n" +
                "\n" +
                "import javax.persistence.Entity;\n" +
                "import javax.persistence.Id;\n" +
                "import javax.persistence.ManyToOne;\n" +
                "import java.util.Date;\n" +
                "\n" +
                "@Include(type = \"artifactVersion\")\n" +
                "@Entity\n" +
                "public class ArtifactVersion {\n" +
                "    @Id\n" +
                "    private String name = \"\";\n" +
                "\n" +
                "    private Date createdAt = new Date();\n" +
                "\n" +
                "    @ManyToOne\n" +
                "    private ArtifactProduct artifact;\n" +
                "}";
    }

    public void compile(String path){
    	 try {
    		 System.out.println("EntityCompiler "+path);
       
			compiler.addSource("com.yahoo.elide.contrib.dynamicconfig.model.PlayerStats", getElideTable(path));
		
       // compiler.addSource("example.models.ArtifactProduct", getArtifactProduct());
       // compiler.addSource("example.models.ArtifactVersion", getArtifactVersion());
        compiledObjects = compiler.compileAll();
        } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public ClassLoader getClassLoader() {
        return compiler.getClassloader();
    }

    public Class<?> getCompiled(String name) {
        return compiledObjects.get(name);
    }
}