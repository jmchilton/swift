/**
 * Code for configuring for a distributed application. The classes contain all the data describing how the application is set up.
 * The base interface is {@link ResourceConfig} - a chunk of data describing any resource in the system.
 * <p>
 * The {@link ResourceConfig} is turned into actual resources using {@link ResourceFactory}. Because the config can
 * reference other configs, we need a {@link DependencyResolver} to translate these reference into actual instances.
 * <p>
 * In a nutshell, {@link ApplicationConfig} is similar to Spring .xml config, and {@link ResourceFactory} is Spring's
 * ApplicationContext. We opted not to use Spring because we needed to edit the configuration in a simple UI and
 * parsing Spring contexts that offer a huge amount of extra functionality we do not need sounded like too much messy work.
 * <p>
 * See {@link ApplicationConfig} for more information how is the application configuration stored.
 */
package edu.mayo.mprc.config;