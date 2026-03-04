package dev.fishies.ranim2.languages.common

interface TreeSitterLanguage {
    /**
     * @return an unmanaged pointer to a TSLanguage
     */
    fun language(): Any

    interface Taggable : TreeSitterLanguage {
        /**
         * @return the tags tree-sitter query for tagging source code
         */
        val tags: String
    }

    interface Highlightable : TreeSitterLanguage {
        /**
         * @return the highlight tree-sitter query for syntax highlighting
         */
        val highlights: String
    }
}
