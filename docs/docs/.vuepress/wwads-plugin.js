module.exports = (options = {}, ctx) => ({
    name: 'wwads-plugin',

    extendsMarkdown: (md) => {
        md.renderer.rules.html_block = (tokens, idx) => {
            const html = tokens[idx].content
            return `<div class="wwads-cn wwads-vertical wwads-sticky" data-id="212" style="max-width:180px"></div>${html}`
        }
    },
})
