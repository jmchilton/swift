/**
 * Core of Swift that does the real work - wraps search engines, serializes/deserializes search definition, persists data,
 * starts a search.
 *
 * These classes are able to take a work packet (for instance one that defines work to be done by Sequest) and
 * either execute it (Mascot) or turn it into command line that can be executed by grid engine.
 */
package edu.mayo.mprc.swift;