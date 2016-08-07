/**
 * Copyright (C) 2012 Inqwell Ltd
 *
 * You may distribute under the terms of the Artistic License, as specified in
 * the README file.
 */

function Typedef(name,
                 alias,
                 fQName,
                 proto,
                 keys,
                 formatStrings,
                 titleStrings,
                 widths,
                 enums,
                 enumSymbols)
{
  this.name           = name;
  this.alias          = alias;
  this.fQName         = fQName;
  this.proto          = proto;
  this.keys           = keys;
  this.formatStrings;
  this.titleStrings;
  this.widths;
  this.enums;
  this.enumSymbols;
}