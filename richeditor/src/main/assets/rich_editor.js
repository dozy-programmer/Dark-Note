/**
 * Copyright (C) 2020 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * See about document.execCommand: https://developer.mozilla.org/en-US/docs/Web/API/Document/execCommand
 */

var RE = {};

RE.currentSelection = {
    "startContainer": 0,
    "startOffset": 0,
    "endContainer": 0,
    "endOffset": 0
};

RE.editor = document.getElementById('editor');

document.addEventListener("selectionchange", function() { RE.backuprange(); });

// Initializations
RE.callback = function() {
    window.location.href = "re-callback://" + encodeURIComponent(RE.getHtml());
}

RE.setHtml = function(contents) {
    RE.editor.innerHTML = decodeURIComponent(contents.replace(/\+/g, '%20'));
}

RE.setRtlDirection = function() {
    RE.editor.dir = "rtl";
    console.log("RTL set");
}

RE.setDefaultDirection = function() {
    RE.editor.dir = "ltr";
    console.log("LTR set");
}

RE.getHtml = function() {
    return RE.editor.innerHTML;
}

RE.getText = function() {
    return RE.editor.innerText;
}

RE.setBaseTextColor = function(color) {
    RE.editor.style.color  = color;
}

RE.setBaseFontSize = function(size) {
    RE.editor.style.fontSize = size;
}

RE.setPadding = function(left, top, right, bottom) {
  RE.editor.style.paddingLeft = left;
  RE.editor.style.paddingTop = top;
  RE.editor.style.paddingRight = right;
  RE.editor.style.paddingBottom = bottom;
}

RE.setBackgroundColor = function(color) {
    document.body.style.backgroundColor = color;
}

RE.setBackgroundImage = function(image) {
    RE.editor.style.backgroundImage = image;
}

RE.setWidth = function(size) {
    RE.editor.style.minWidth = size;
}

RE.setHeight = function(size) {
    RE.editor.style.height = size;
}

RE.setTextAlign = function(align) {
    RE.editor.style.textAlign = align;
}

RE.setVerticalAlign = function(align) {
    RE.editor.style.verticalAlign = align;
}

RE.setPlaceholder = function(placeholder) {
    RE.editor.setAttribute("placeholder", placeholder);
}

RE.setInputEnabled = function(inputEnabled) {
    RE.editor.contentEditable = String(inputEnabled);
}

RE.undo = function() {
    document.execCommand('undo', false, null);
}

RE.redo = function() {
    document.execCommand('redo', false, null);
}

RE.setBold = function() {
    document.execCommand('bold', false, null);
}

RE.setItalic = function() {
    document.execCommand('italic', false, null);
}

RE.setSubscript = function() {
    document.execCommand('subscript', false, null);
}

RE.setSuperscript = function() {
    document.execCommand('superscript', false, null);
}

RE.setStrikeThrough = function() {
    document.execCommand('strikeThrough', false, null);
}

RE.setUnderline = function() {
    document.execCommand('underline', false, null);
}

RE.setBullets = function() {
    document.execCommand('insertUnorderedList', false, null);
}

RE.setNumbers = function() {
    document.execCommand('insertOrderedList', false, null);
}

RE.setTextColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('foreColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setTextBackgroundColor = function(color) {
    RE.restorerange();
    document.execCommand("styleWithCSS", null, true);
    document.execCommand('hiliteColor', false, color);
    document.execCommand("styleWithCSS", null, false);
}

RE.setFontSize = function(fontSize){
    document.execCommand("fontSize", false, fontSize);
}

RE.setHeading = function(heading) {
    document.execCommand('formatBlock', false, '<h'+heading+'>');
}

RE.setIndent = function() {
    document.execCommand('indent', false, null);
}

RE.setOutdent = function() {
    document.execCommand('outdent', false, null);
}

RE.setJustifyLeft = function() {
    document.execCommand('justifyLeft', false, null);
}

RE.setJustifyCenter = function() {
    document.execCommand('justifyCenter', false, null);
}

RE.setJustifyRight = function() {
    document.execCommand('justifyRight', false, null);
}

RE.insertImageWH = function(url, alt, width, height) {
    var html = '<img src="' + url + '" alt="' + alt + '" width="' + width + '" height="' + height +'"/><br>';
    RE.insertHTML(html);
}

RE.insertYoutubeVideoWH = function(url, width, height) {
    var html = '<iframe width="' + width + '" height="' + height + '" src="' + url + '" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope"/><br>'
    RE.insertHTML(html);
}

RE.insertHTML = function(html) {
    RE.restorerange();
    document.execCommand('insertHTML', false, html);
}

RE.prepareInsert = function() {
    RE.backuprange();
}

RE.backuprange = function(){
    var selection = window.getSelection();
    if (selection.rangeCount > 0) {
      var range = selection.getRangeAt(0);
      RE.currentSelection = {
          "startContainer": range.startContainer,
          "startOffset": range.startOffset,
          "endContainer": range.endContainer,
          "endOffset": range.endOffset
      };
    }
}

RE.restorerange = function(){
    var selection = window.getSelection();
    selection.removeAllRanges();
    var range = document.createRange();
    range.setStart(RE.currentSelection.startContainer, RE.currentSelection.startOffset);
    range.setEnd(RE.currentSelection.endContainer, RE.currentSelection.endOffset);
    selection.addRange(range);
}

RE.enabledEditingItems = function(e) {
    var items = [];
    if (document.queryCommandState('bold')) {
        items.push('bold');
    }
    if (document.queryCommandState('italic')) {
        items.push('italic');
    }
    if (document.queryCommandState('subscript')) {
        items.push('subscript');
    }
    if (document.queryCommandState('superscript')) {
        items.push('superscript');
    }
    if (document.queryCommandState('strikeThrough')) {
        items.push('strikeThrough');
    }
    if (document.queryCommandState('underline')) {
        items.push('underline');
    }
    if (document.queryCommandState('insertOrderedList')) {
        items.push('orderedList');
    }
    if (document.queryCommandState('insertUnorderedList')) {
        items.push('unorderedList');
    }
    if (document.queryCommandState('justifyCenter')) {
        items.push('justifyCenter');
    }
    if (document.queryCommandState('justifyFull')) {
        items.push('justifyFull');
    }
    if (document.queryCommandState('justifyLeft')) {
        items.push('justifyLeft');
    }
    if (document.queryCommandState('justifyRight')) {
        items.push('justifyRight');
    }
    if (document.queryCommandState('insertHorizontalRule')) {
        items.push('horizontalRule');
    }
    var formatBlock = document.queryCommandValue('formatBlock');
    if (formatBlock.length > 0) {
        items.push(formatBlock);
    }

    window.location.href = "re-state://" + encodeURI(items.join(','));
}

RE.focus = function() {
    var range = document.createRange();
    range.selectNodeContents(RE.editor);
    range.collapse(false);
    var selection = window.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
    RE.editor.focus();
}

RE.setSelection = function(position) {
    setCursorAtPosition(RE.editor, position);
    RE.getAbsoluteCaretYPosition()
}

function setCursorAtPosition(node, index) {
  let range = document.createRange();
  let selection = window.getSelection();
  let currentPos = 0;
  let found = false;

  function searchNode(node) {
    if (node.nodeType === Node.TEXT_NODE) {
      let text = node.textContent;
      if (currentPos + node.length >= index) {
        range.setStart(node, index - currentPos);
        range.collapse(true);
        found = true;
      }
      else {
        currentPos += node.length;
      }
    } else {
      for (let child of node.childNodes) {
        if (found) break;
        searchNode(child);
      }
    }
  }

  searchNode(node);
  selection.removeAllRanges();
  selection.addRange(range);
}

RE.getAbsoluteCaretYPosition = function() {
  var y = 0;
  var sel = window.getSelection();
  if (sel.rangeCount) {
    var range = sel.getRangeAt(0);

    if (range.getClientRects) {
      var rects = range.getClientRects();
      if (rects.length > 0) {
        y = rects[0].top;
      }
    }

    y += window.pageYOffset;
  }
  window.scrollTo(0, y);
};

RE.blurFocus = function() {
    RE.editor.blur();
}

RE.removeFormat = function() {
    RE.restorerange();
    document.execCommand('removeFormat', false, null);
    selection.removeAllRanges();
}

// Event Listeners
RE.editor.addEventListener("input", RE.callback);
RE.editor.addEventListener("keyup", function(e) {
    var KEY_LEFT = 37, KEY_RIGHT = 39;
    if (e.which == KEY_LEFT || e.which == KEY_RIGHT) {
        RE.enabledEditingItems(e);
    }
});
var isSingleClick = Boolean(true)
RE.editor.addEventListener("click",  function(e) {
    if(!isSingleClick) return;
     //console.log("click for -> " + e.target.tagName);
    // Check if the clicked element is an image and not text
    if (e.target.tagName === 'IMG') {
        var imageSrc = event.target.src;
        if(imageSrc.startsWith("file://"))
            imageSrc = imageSrc.replace("file://", "");
        DarkNote.getImagePath(imageSrc);
    }
    else {
        RE.enabledEditingItems(e);
    }
});

RE.editor.oncontextmenu = function(e) {
    isSingleClick = Boolean(false);
    const imageElement = e.target;
    const source = imageElement.src.replace("file://", "")
    const reconstructedHTML = `<img src="${source}" alt="${imageElement.alt}" width="${imageElement.width}" height="${imageElement.height}">`;
    if (e.target.tagName === 'IMG') {
        const imageSrc = event.target.src;
        var width = Number(event.target.width);
        var height = Number(event.target.height);
        DarkNote.getImagePath(imageSrc, reconstructedHTML, width, height);
    }
    else {
        RE.enabledEditingItems(e);
    }
    isSingleClick = Boolean(true);
};