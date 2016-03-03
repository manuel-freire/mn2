====================================
Building icons for use in ManyNets 2
====================================

1. Edit your icons in SVG using, for instance, Inkskape.   
   => sources/icons.svg

2. Export the icons to PNG format, using default resolution
   Using Inkscape, select all icons and choose "export selected" to create
   => sources/icons.png

3. Open PNG iconsheet and cut out individual icons into files
   Using Gimp,
     3.1 Place guidelines to delimit grid (makes selection more precise)
     3.2 For each icon to cut, use select tool to select, and
        Ctrl+C (copy), Ctrl+Shift+V (new image with), Ctrl+S (save)
   => individual/<icon-name>.png
   Could be automated, but the icon names would still need to be entered by hand
  
4. Build into 48x48 and 24x24, enabled and disabled icon variants, using
   [individual/build.sh], which requires imagemagick to be installed.
   Should be called from *within* the 'individual' directory
   => build/<icon-name>[24][_disabled].png

5. Install into 
   => manynets-app/src/edu/umd/cs/hcil/manynets/icons/<icon-name><version>.png
   [individual/install.sh] does this for you.
   Should be called from *within* the 'individual' directory

