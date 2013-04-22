# Pinion: Assets management for Dropwizard based on Sprockets

Pinion is a Java servlet for compiling, concatinating and serving assets.  It uses Sprockets style dependency management and currently supports Less, Coffeescript, cssmin, uglifier and the Closure compiler.  Pinion is primary built for use with Dropwizard but the core is a raw servlet that should work in any servlet container, although you may need to write some configuration code to do so. 

Pinion is in an early stage of development, do not use it for production yet.  All feature/pull/bug requests welcome!

# Installation #

Add the following to your pom.xml:

WARNING: this isn't in maven yet...you'll have to clone and built with maven for now.

```xml
<dependency>
  <groupId>org.pinion</groupId>
  <artifactId>pinion-dropwizard</artifactId>
  <version>0.0.1</version>
</dependency>
```

Take a look at the pinion-example module for example usage configuration.

In your dropwizard configuration class, add the following:

```java
  @Valid
  @NotNull
  @JsonProperty
  private PinionConfiguration pinion = new PinionConfiguration();

  public PinionConfiguration getPinionConfiguration() {
    return pinion;
  }
```

In your dropwizard service class create and add the pinion bundle:

```java
  //The string passed to PinionBundle<> will determine where the servlet will be mounted, in this example
  //and by default it will be mounted at "assets/".  If you want to serve from the root you need to move
  //the dropwizard service to another location.  See the dropwizard documenation.
  private final PinionBundle<ExampleConfiguration> pinionBundle = new PinionBundle<ExampleConfiguration>("assets/") {
    @Override
    public PinionConfiguration getPinionConfiguration(ExampleConfiguration configuration) {
      return configuration.getPinionConfiguration();
    }
  };

  @Override
  public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
    bootstrap.addBundle(pinionBundle);
  }

```

# Configuration #
  
  The servlet root is set when the bundle is created, see the installation docs above.
  
  Other configuration is done in your dropwizard yaml config file:

```yaml
pinion:

  #Path where assets are located in your project structure relative to classloader root Default: assets/
  assetPath: assets/

  #Version of your application.  Used for fingerprinting file.
  appVersion: 0.0.1

  #Compress javascript files?  Default: true
  jsCompress: true

  #Compress css files? Default: true
  cssCompress: true

  #Which javascript compressor to use, options are 'uglifier' or 'closure'
  jsCompressor: uglifier

  coffee:
    #TODO docs
  less:
    #TODO docs
  closure:
    #TODO docs
  cssmin:
    #TODO docs
```

# Usage #

## Directive Processor ##

The directive processor allows you to define concatination and dependency rules in your source files.  When a directive is found in an asset the files matching the GLOB expression are processed and concatinated into the file at the location of the directive.  

This is handled very similarly to sprockets with the exception that only a single directive, 'require' is supported and it allows GLOB syntax instead of having separate directives for things like 'require_tree'.  

Also like sprockets, directives must be in a comment block at the top of a file ('//' and '/* */' style comments supported) and there must be an '=' at the start of the line (ignoring comment indicators).    

For example:

application.js
```javascript
// application.js
//= require *.js
//= require *.js*
//= require **.js
```

application.js
```javascript
/* application.js
 *= require *.js
 *= require *.js*
 *= require **.js
*/
```

## Caching ##

Assets are cached in an in memory store (file store also planned).  Assets are only rebuilt when dirty as determine by file existance, modification time, and a digest of the file's contents.  


# TODO #

## Stuff Prior to a 0.1 release ##

- Additional test converage
- Finish exposing all engine configuration options
- Release to a maven repo
- Bugs
- Clean up code structure a bit
- Leak/performance test

## Planned features ##

- Support node.js in addition to Rhino.  Rhino is slow.
- File based cache in addition to current in memory cache
- File fingerprinting for caching.  Requires view template helper support (what template engines to support?)
- Performance testing / optimization
- Optional SASS support (requires pulling in all of JRuby or an external ruby call)

# License #

MIT
